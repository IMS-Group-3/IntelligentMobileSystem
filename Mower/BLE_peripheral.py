import sys
import tty
import termios
import threading
from pybleno import Bleno, BlenoPrimaryService, Characteristic, Descriptor

# Function to read a line of input from the terminal
def read_input_line():
    return sys.stdin.readline().rstrip()

class KeyInputCharacteristic(Characteristic):
    def __init__(self):
        Characteristic.__init__(
            self, {
                "uuid": "12345678-1234-5678-1234-56789abcdef1",
                "properties": ["read", "notify"],
                "value": None,
            }
        )
        self._value = ""
        self._updateValueCallback = None

    def onReadRequest(self, offset, callback):
        print("Read request received")
        callback(Characteristic.RESULT_SUCCESS, self._value.encode())

    def onSubscribe(self, maxValueSize, updateValueCallback):
        print("Subscribed to key input updates")
        self._updateValueCallback = updateValueCallback

    def onUnsubscribe(self):
        print("Unsubscribed from key input updates")
        self._updateValueCallback = None

    def publish_key_input(self, input_line):
        self._value = input_line
        if self._updateValueCallback:
            print(f"Publishing input: '{input_line}'")
            self._updateValueCallback(self._value.encode())

def read_inputs_thread(key_input_characteristic):
    while True:
        input_line = read_input_line()
        key_input_characteristic.publish_key_input(input_line)

# Create a KeyInputCharacteristic instance
key_input_characteristic = KeyInputCharacteristic()

# Create a Bleno instance
bleno = Bleno()

# Set the Bleno advertising parameters
def onStateChange(state):
    if state == "poweredOn":
        bleno.startAdvertising("RaspberryPiZero", ["12345678-1234-5678-1234-56789abcdef0"])
    else:
        bleno.stopAdvertising()

bleno.on("stateChange", onStateChange)

# Set the GATT server services and characteristics
def onAdvertisingStart(error):
    if not error:
        bleno.setServices([
            BlenoPrimaryService({
                "uuid": "12345678-1234-5678-1234-56789abcdef0",
                "characteristics": [
                    key_input_characteristic
                ]
            })
        ])

bleno.on("advertisingStart", onAdvertisingStart)

# Start the Bleno service
bleno.start()

# Set terminal to the normal mode
termios.tcsetattr(sys.stdin.fileno(), termios.TCSADRAIN, termios.tcgetattr(sys.stdin.fileno()))

# Start the thread to read inputs from the terminal
key_input_thread = threading.Thread(target=read_inputs_thread, args=(key_input_characteristic,))
key_input_thread.start()

print("Press Ctrl-C to stop the application...")

try:
    while True:
        pass
except KeyboardInterrupt:
    # Set terminal to the raw mode
    tty.setraw(sys.stdin.fileno())
    key_input_thread.join()
    bleno.stopAdvertising()
    bleno.disconnect()
    print("Application stopped.")
