import serial
import time
import sys
import json
import os
import threading
from IMS_polygon import IMS_Polygon
from enum import Enum
from IMS_picture_handler import IMS_picture_handler as IMS_picture
from IMS_bluetooth_controller import BluetoothController as IMS_BLE
from IMS_HTTP_requests import IMS_HTTP_request_client as HTTP_client
from IMS_MQTT_listener import IMS_MQTT_listener as MQTT_Listener
import math


# Enum class that keeps tabs on the mower state and can return its value in both integer and character form
class Mower_state(Enum):
    Off = ' ', 0
    Inside = 'I', 1
    Outside = 'O', 2
    Manual = 'M', 3

    def __init__(self, string_repr, numeric_value):
        self._value_ = string_repr
        self.numeric_value = numeric_value

    def __str__(self):
        return self.value

    def __repr__(self):
        return f"<mower_state.{self.name}>"


class IMS_arduino_communicator:

    def __init__(self, num_ports=2):
        # Create a client to handle HTTP requests
        self.http_client = HTTP_client(base_url="http://16.16.68.202/")

        # Create a controller for bluetooth connection
        self.bt_controller = IMS_BLE(self.bluetooth_callback_wrapper)

        # Create a handler for image processing
        self.img_handler = IMS_picture()

        # Create a listener to the MQTT connection
        self.MQTT_listener = MQTT_Listener(self.mqtt_callback)

        # Create an object to track mower state. And variables that track the engine PWM values
        self.mower_state = Mower_state.Manual
        self.mower_session_start = None
        self.current_coordinates = None
        self.right_speed = 0
        self.left_speed = 0

        # Variable that changes the writing rate to the mower/Backend (sleep time in seconds)
        self.write_speed_mower = 0.2
        self.write_speed_backend = 0.5
        self.last_write_backend = None

        # Create objects to keep the serial communication open
        self.serial_ports = self.get_serial_ports(num_ports)
        self.sers = [
            serial.Serial(port, 115200, timeout=1)
            for port in self.serial_ports
        ]

    @staticmethod
    def get_serial_ports(num_ports):
        found_ports = []
        if os.name == 'posix':  # For Linux/OSX
            base_ports = ["/dev/ttyUSB", "/dev/ttyACM"]
        elif os.name == 'nt':  # For Windows
            base_ports = ["COM"]

        for base_port in base_ports:
            for i in range(0, 10):
                if len(found_ports) >= num_ports:
                    break
                try:
                    serial_port = base_port + str(i)
                    ser = serial.Serial(serial_port, 115200, timeout=1)
                    ser.close()
                    found_ports.append(serial_port)
                except:
                    pass

        if len(found_ports) < num_ports:
            raise Exception(f"Could not find {num_ports} Arduinos.")
        return found_ports

    def read_arduino(self, ser, arduino_id):
        while self.running:
            if ser.in_waiting:
                received_str = ser.readline().decode('utf-8',
                                                     errors='ignore').strip()
                #print(f"{arduino_id}: {received_str}")
                if (arduino_id == 1):
                    if (received_str == "Collision"):
                        print("Collision avoidance event occured")
                        if (self.current_coordinates != None):
                            self.send_avoidance_image()
                            self.send_coordinates(is_collision=True)
                elif (arduino_id == 2):
                    try:
                        if (self.get_mower_state()
                                == Mower_state.Inside.numeric_value
                                or self.get_mower_state()
                                == Mower_state.Outside.numeric_value):
                            # Update current coordinates
                            self.current_coordinates = self.get_mower_position(
                                received_str)
                            #Verify if inside boundary
                            self.verify_mower_position(
                                self.current_coordinates)
                            # Send new coordinates to backend
                            self.send_coordinates()

                    except Exception as e:
                        #print(f"Error reading coordinates: {e}")
                        pass
            time.sleep(0.1)

    @staticmethod
    def get_mower_position(position_string):
        # Full string should look like: "Position,x:pos_x,y:pos_y,z:pos_z/n" where pos_x, pos_y, pos_z are integer values
        position = position_string.split(",")
        pos_x = int(position[1].split(":")[1])
        pos_y = int(position[2].split(":")[1])
        #print(f"get_mower_position: pos_x:{pos_x}, pos_y:{pos_y}")
        current_coordinates = [pos_x, pos_y]
        return current_coordinates

    def verify_mower_position(self, current_coordinates: list[int]):
        polygon_instance = IMS_Polygon()
        if (polygon_instance.isInside(IMS_Polygon.standard_points,
                                      current_coordinates)):
            self.set_mower_state(1)

        else:
            self.set_mower_state(2)

    def set_mower_state(self, new_mower_state):
        #print(f"set_mower_state({newMowerState})")
        if (new_mower_state == 0):
            self.mower_state = Mower_state.Off

        elif (new_mower_state == 1):
            if (self.mower_state.numeric_value != 1):
                print("Mower is now inside!")
            self.mower_state = Mower_state.Inside

        elif (new_mower_state == 2):
            if (self.mower_state.numeric_value != 2):
                print("Mower is now Outside!")
            self.mower_state = Mower_state.Outside

        elif (new_mower_state == 3):
            self.mower_state = Mower_state.Manual

    def get_mower_state(self):
        return self.mower_state.numeric_value

    def calculate_engine_speeds(self,
                                angle,
                                strength,
                                deadzone=10,
                                min_speed=100,
                                max_speed=255):
        if strength < deadzone:
            self.left_speed = 0
            self.right_speed = 0
            return

        angle_rad = math.radians(angle)
        normalized_strength = strength / 100.0

        sin_angle = math.sin(angle_rad)
        cos_angle = math.cos(angle_rad)

        if sin_angle >= 0:  # Forward
            left_strength = normalized_strength * (1 + cos_angle)
            right_strength = normalized_strength * (1 - cos_angle)
        else:  # Reverse
            left_strength = -normalized_strength * (1 + cos_angle)
            right_strength = -normalized_strength * (1 - cos_angle)

        # Multiply by +/- 255 based on the direction
        left_pwm = int(left_strength * 255)
        right_pwm = int(right_strength * 255)

        # Limit PWM values to the range of -255 to 255
        left_pwm = max(-255, min(left_pwm, 255))
        right_pwm = max(-255, min(right_pwm, 255))

        # Inverse motor direction based on forward or reverse movement
        self.left_speed = left_pwm
        self.right_speed = -right_pwm  # right engine is mounted in reverse
        print(
            f"right pwm: {self.right_speed}, left pwm: {self.left_speed}. sin({angle})={sin_angle}, cos({angle})={cos_angle}"
        )

    def calculate_engine_speed_old(self,
                                   angle,
                                   strength,
                                   deadzone=10,
                                   min_speed=100,
                                   max_speed=255):
        if strength < deadzone:
            self.left_speed = 0
            self.right_speed = 0
            return

        angle_rad = math.radians(angle)
        normalized_strength = strength / 100.0

        sin_angle = math.sin(angle_rad)
        cos_angle = math.cos(angle_rad)

        # Determine if we are going forward or backward based on the sign of sin_angle
        if sin_angle >= 0:  # Forward
            if cos_angle > 0:
                left_wheel_speed = normalized_strength
                right_wheel_speed = normalized_strength * (1 - cos_angle)
            else:
                left_wheel_speed = normalized_strength * (1 + cos_angle)
                right_wheel_speed = normalized_strength
            min_speed_for_calculation = min_speed
        else:  # Reverse
            if cos_angle > 0:
                left_wheel_speed = -normalized_strength * (1 + cos_angle)
                right_wheel_speed = -normalized_strength
            else:
                left_wheel_speed = -normalized_strength
                right_wheel_speed = -normalized_strength * (1 - cos_angle)
            min_speed_for_calculation = -min_speed

        # Convert wheel speeds to motor speed range
        left_pwm = int(left_wheel_speed *
                       (max_speed - min_speed_for_calculation) +
                       min_speed_for_calculation)
        right_pwm = int(right_wheel_speed *
                        (max_speed - min_speed_for_calculation) +
                        min_speed_for_calculation)

        # Inverse motor direction based on forward or reverse movement
        self.left_speed = left_pwm
        self.right_speed = -right_pwm  # right engine is mounted in reverse
        print(
            f"right pwm:{self.right_speed}, left pwm:{self.left_speed}. sin({angle})={sin_angle}, cos({angle})={cos_angle}"
        )

    def write_mower(self):
        while (self.running):
            time.sleep(self.write_speed_mower)
            if (self.get_mower_state() == 3):
                input_str = (
                    f"1,<{self.mower_state},{self.right_speed},{self.left_speed}>"
                )
                #print(f"writing: mower_state:{self.mower_state}, right_pwm:{self.pwm_right}, left_pwm:{self.pwm_left}")
            else:
                input_str = (f"1,<{self.mower_state}>")

            arduino_id, message = input_str.split(',', 1)
            try:
                arduino_id = int(arduino_id)
                self.sers[arduino_id - 1].write(message.encode())
            except (IndexError, ValueError):
                print("Invalid Arduino ID. Please use a valid ID.")

            #print(f"Sent to Arduino {arduino_id}:{message}")

    def bluetooth_callback(self, angle, strength):
        self.calculate_engine_speeds(angle, strength)

    def bluetooth_callback_wrapper(self, angle, strength):
        self.bluetooth_callback(angle, strength)

    #TODO Fix Blocking while loop
    def send_avoidance_image(self):

        img_stream = self.img_handler.take_picture_and_compress(quality=75)
        base64_img = self.img_handler.encode_image_to_base64(img_stream)
        #print(f"Collision occured image sent: {base64_img[:100]}")
        json_payload = {
            "encodedImage": base64_img,
            "x": self.current_coordinates[0],
            "y": self.current_coordinates[1]
        }
        timeout = 0
        while (timeout < 10):
            try:
                self.http_client.send_post_request(endpoint="image",
                                                   data=json_payload)
                break
            except Exception as e:
                print(f"Error sending image to backend: {e}")
                timeout += 1

    def send_coordinates(self, is_collision=False):
        # If its the first message set the session start time
        if (self.mower_session_start == None):
            self.mower_session_start = time.strftime("%Y-%m-%d %H:%M:%S")

            # check if enough time has passed since last message was sent to backend
        if (self.http_backend_request_delay()):
            json_payload = {
                "startTime": self.mower_session_start,
                "x": self.current_coordinates[0],
                "y": self.current_coordinates[1],
                "timestamp": time.strftime("%Y-%m-%d %H:%M:%S"),
                "collisionOcurred": 1 if (is_collision) else 0
            }
            try:
                #print(f"sending collision is_collision={is_collision}")
                self.http_client.send_post_request(endpoint="paths/position",
                                                   data=json_payload)
            except Exception as e:
                print(
                    f"Error occured while sending coordinates to backend: {e}")

    def http_backend_request_delay(self, request_interval=0.5):
        if (self.last_write_backend == None):
            self.last_write_backend = time.time()
            return True
        else:
            current_time = time.time()
            elapsed_time = current_time - self.last_write_backend

            if elapsed_time >= request_interval:
                return True
            else:
                return False

    def mqtt_callback(self, json_data):
        try:
            command = json.loads(json_data)["command"]

            if (command == "turn_off"):
                self.mower_session_start = None
                self.set_mower_state(Mower_state.Off.numeric_value)
                print("mqtt_ turn off")
            elif (command == "autonomous"):
                self.set_mower_state(Mower_state.Inside.numeric_value)
                print("mqtt_ Autonomous")
            elif (command == "manual"):
                self.mower_session_start = None
                self.set_mower_state(Mower_state.Manual.numeric_value)
                print("mqtt_ Manual")
        except json.JSONDecodeError as e:
            print(f"Error decoding JSON: {e}")

    def start(self):
        try:
            print(f"Connected to {self.serial_ports}. Press Ctrl+C to exit.")
            time.sleep(1)

            # Flag that shows when threads are running
            self.running = True

            # Start threads for reading Serial from the Arduinos
            read_threads = [
                threading.Thread(target=self.read_arduino, args=(ser, idx + 1))
                for idx, ser in enumerate(self.sers)
            ]
            for thread in read_threads:
                thread.start()

            # Start thread for writing to Arduinos
            write_thread = threading.Thread(target=self.write_mower)
            write_thread.start()

            # Start thread for the Bluetooth Communication
            bt_controller_thread = threading.Thread(
                target=self.bt_controller.run)
            bt_controller_thread.start()

            # Start a thread for the MQTT Listener
            mqtt_controller_thread = threading.Thread(
                target=self.MQTT_listener.run)
            mqtt_controller_thread.start()

            # Sleep in the main function to give processing time for the threads
            while self.running:
                time.sleep(5)

        except KeyboardInterrupt:
            print("\nClosing the serial communication.")
            self.running = False
            write_thread.join()
            self.bt_controller.stop()
            bt_controller_thread.join()
            for thread in read_threads:
                thread.join()
            for ser in self.sers:
                ser.close()

        except Exception as e:
            print(f"Error: {e}")


if __name__ == "__main__":
    communicator = IMS_arduino_communicator()
    communicator.start()
