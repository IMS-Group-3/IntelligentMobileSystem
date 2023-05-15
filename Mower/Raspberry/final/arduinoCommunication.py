import serial
import time
import sys
import os
import threading
from IMS_polygon import IMS_Polygon
from enum import Enum
from IMS_picture_handler import IMS_picture_handler as IMS_picture
from IMS_bluetooth_controller import BluetoothController as IMS_BLE
import math


# Enum class that keeps tabs on the mower state and can return its value in both integer and character form
class MowerState(Enum):
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
        return f"<Status.{self.name}>"


class IMS_arduino_communicator:

    def __init__(self, num_ports=2):
        self.bt_controller = IMS_BLE()
        self.mowerState = MowerState.Manual
        self.pwm_left = 0
        self.pwm_right = 0
        self.write_speed = 0.2
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
                        send_complete = False
                        time_out = 0
                        while (not send_complete):
                            send_complete = IMS_picture.send_picture(
                                IMS_picture.take_picture_and_compress)
                            if (time_out < 10):
                                time_out += 1
                            else:
                                break
                elif (arduino_id == 2):
                    try:
                        if (self.get_mower_state() == 1
                                or self.get_mower_state() == 2):
                            self.verify_mower_position(
                                self.get_mower_position(received_str))
                        elif (self.get_mower_state() == 3):
                            self.set_mower_pwm()

                    except Exception as e:
                        print(f"Error reading coordinates: {e}")
            time.sleep(0.1)

    @staticmethod
    def get_mower_position(position_string):
        # Full string should look like: "Position,x:pos_x,y:pos_y,z:pos_z/n"
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

    def set_mower_state(self, newMowerState):
        #print(f"set_mower_state({newMowerState})")
        if (newMowerState == 0):
            self.mowerState = MowerState.Off

        elif (newMowerState == 1):
            if (self.mowerState.numeric_value != 1):
                print("Mower is now inside!")
            self.mowerState = MowerState.Inside

        elif (newMowerState == 2):
            if (self.mowerState.numeric_value != 2):
                print("Mower is now Outside!")
            self.mowerState = MowerState.Outside

        elif (newMowerState == 3):
            self.mowerState = MowerState.Manual

    def get_mower_state(self):
        return self.mowerState.numeric_value

    def set_mower_pwm(self):
        bt_values = self.bt_controller.get_controller_values()
        angle = bt_values["angle"]
        strength = bt_values["strength"]
        self.pwm_left, self.pwm_right = self.calculate_engine_speeds(
            angle, strength)

    def calculate_engine_speeds(angle, strength, deadzone_threshold=10):
        angle_rad = math.radians(angle)
        normalized_strength = strength / 100.0

        # Check if the strength is within the deadzone
        if strength < deadzone_threshold:
            left_speed = 0
            right_speed = 0
        else:
            # Calculate the differential speeds for the left and right engines
            left_speed = math.sin(angle_rad + math.pi / 4)
            right_speed = math.sin(angle_rad - math.pi / 4)

            # Scale the speeds according to the given strength
            left_speed = int(100 + (left_speed * 155 * normalized_strength))
            right_speed = int(100 + (right_speed * 155 * normalized_strength))

        return left_speed, right_speed

    def write_mower(self):
        while (self.running):
            time.sleep(self.write_speed)
            if (self.get_mower_state() == 3):
                input_str = (
                    f"1,{self.mowerState},{self.pwm_left},{self.pwm_right}")
            else:
                input_str = (f"1,{self.mowerState}")
            arduino_id, message = input_str.split(',', 1)
            try:
                arduino_id = int(arduino_id)
                self.sers[arduino_id - 1].write(message.encode())
            except (IndexError, ValueError):
                print("Invalid Arduino ID. Please use a valid ID.")

            #print(f"Sent to Arduino {arduino_id}:{message}")

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
