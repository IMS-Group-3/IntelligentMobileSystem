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
        self.pwm_left = 0
        self.pwm_right = 0

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
            return 0, 0

        strength = strength / 100  # Normalize the strength

        angle_rad = math.radians(angle)
        adjusted_angle = (angle_rad - math.pi / 2) % (2 * math.pi)
        if adjusted_angle > math.pi:
            adjusted_angle -= 2 * math.pi

        linear_velocity = strength * math.cos(adjusted_angle)
        angular_velocity = strength * math.sin(adjusted_angle)

        left_wheel_speed = linear_velocity - angular_velocity
        right_wheel_speed = linear_velocity + angular_velocity

        # Convert wheel speeds to PWM values
        left_pwm = int(min_speed * left_wheel_speed + (max_speed - min_speed) *
                       (left_wheel_speed / 2))
        # The right engine is mounted backwards and needs its PWM value to be inverted
        right_pwm = -int(min_speed * right_wheel_speed +
                         (max_speed - min_speed) * (right_wheel_speed / 2))
        #print(f"left pwm:{left_pwm}, right pwm:{right_pwm}")

        self.left_speed = left_pwm
        self.right_speed = right_pwm

    def write_mower(self):
        while (self.running):
            time.sleep(self.write_speed_mower)
            if (self.get_mower_state() == 3):
                input_str = (
                    f"1,{self.mower_state},{self.pwm_right},{self.pwm_left}")
                #print(f"writing: mower_state:{self.mower_state}, right_pwm:{self.pwm_right}, left_pwm:{self.pwm_left}")
            else:
                input_str = (f"1,{self.mower_state}")
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
        img_stream = self.img_handler.take_picture_and_compress
        base64_img = self.img_handler.encode_image_to_base64(img_stream)
        json_payload = {"encodedImage", base64_img}
        timeout = 0
        while (timeout < 10):
            try:
                self.http_client.send_post_request(endpoint="image",
                                                   data=json_payload)
            except Exception as e:
                print(f"Error sending image to backend: {e}")
                timeout += 1

    def send_coordinates(self, is_collision=False):
        # If its the first message set the session start time
        if (self.mower_session_start == None):
            self.mower_session_start = time.strftime("%H:%M")

            # check if enough time has passed since last message was sent to backend
        if (self.http_backend_request_delay()):
            json_payload = {
                "startTime": self.mower_session_start,
                "x": self.current_coordiantes[0],
                "y": self.current_coordiantes[1],
                "timestamp": "%Y%m%d %H%M%S",
                "collisionOccured": is_collision
            }
            try:
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
                self.set_mower_state(Mower_state.Off.numeric_value)
                print("mqtt_ turn off")
            elif (command == "autonomous"):
                self.set_mower_state(Mower_state.Inside.numeric_value)
                print("mqtt_ Autonomous")
            elif (command == "manual"):
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