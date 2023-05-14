import serial
import time
import sys
import os
import threading


def get_serial_ports(num_ports=2):
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


def read_arduino(ser, arduino_id):
    while True:
        if ser.in_waiting:
            received_str = ser.readline().decode('utf-8',
                                                 errors='ignore').strip()
            print(f"Received from Arduino {arduino_id}: {received_str}")
        time.sleep(0.1)


def main():
    try:
        serial_ports = get_serial_ports()
        ser1 = serial.Serial(serial_ports[0], 115200, timeout=1)
        ser2 = serial.Serial(serial_ports[1], 115200, timeout=1)
        print(
            f"Connected to {serial_ports[0]} and {serial_ports[1]}. Press Ctrl+C to exit."
        )
        time.sleep(2)

        read_thread1 = threading.Thread(target=read_arduino, args=(ser1, 1))
        read_thread2 = threading.Thread(target=read_arduino, args=(ser2, 2))
        read_thread1.daemon = True
        read_thread2.daemon = True
        read_thread1.start()
        read_thread2.start()

        while True:
            input_str = input(
                "Enter a message to send to the Arduinos (format: '<arduino_id> <message>'): "
            )
            arduino_id, message = input_str.split(' ', 1)
            if arduino_id == '1':
                ser1.write(message.encode())
            elif arduino_id == '2':
                ser2.write(message.encode())
            else:
                print("Invalid Arduino ID. Please use 1 or 2.")
                continue

            print(f"Sent to Arduino {arduino_id}: {message}")

    except KeyboardInterrupt:
        print("\nClosing the serial communication.")
        ser1.close()
        ser2.close()

    except Exception as e:
        print(f"Error: {e}")


if __name__ == "__main__":
    main()
