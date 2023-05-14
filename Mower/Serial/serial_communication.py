import serial
import time
import sys
import os

#Make sure you connect to the correct COM port, this just selects the first one it finds :)
def get_serial_port():
    if os.name == 'posix':  # For Linux/OSX
        base_ports = ["/dev/ttyUSB", "/dev/ttyACM"]
    elif os.name == 'nt':  # For Windows
        base_ports = ["COM"]

    for base_port in base_ports:
        for i in range(0, 10):
            try:
                serial_port = base_port + str(i)
                ser = serial.Serial(serial_port, 115200, timeout=1)
                ser.close()
                return serial_port
            except:
                pass

    raise Exception("No Arduino found.")

def main():
    try:
        serial_port = get_serial_port()
        ser = serial.Serial(serial_port, 115200, timeout=1)
        print(f"Connected to {serial_port}. Press Ctrl+C to exit.")
        time.sleep(2)

        while True:
            input_str = input("Enter a message to send to the Arduino: ")
            ser.write(input_str.encode())
            print(f"Sent: {input_str}")

            time.sleep(1)

            while ser.in_waiting:
                received_str = ser.readline().decode().strip()
                print(f"Received: {received_str}")

    except KeyboardInterrupt:
        print("\nClosing the serial communication.")
        ser.close()

    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    main()
