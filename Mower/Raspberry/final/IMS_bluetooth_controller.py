from pybleno import Bleno, BlenoPrimaryService, Characteristic


class BluetoothController:

    def __init__(self, callback):
        self.bluetooth_callback = callback  #Stores the callback function
        self.driving_control_characteristic = self.DrivingControlCharacteristic(
            self)
        self.bleno = Bleno()
        self.bleno.on("stateChange", self.on_state_change)
        self.bleno.on("disconnect", self.on_disconnect)
        self.bleno.on("advertisingStart", self.on_advertising_start)

    class DrivingControlCharacteristic(Characteristic):

        def __init__(self, parent):
            Characteristic.__init__(
                self, {
                    "uuid": "12345678-1234-5678-1234-56789abcdef2",
                    "properties": ["read", "write"],
                    "value": None,
                })
            self._value = ""
            self._parent = parent  #Stores the BluetoothController instance

        def onReadRequest(self, offset, callback):
            print("Read request received for driving control")
            callback(Characteristic.RESULT_SUCCESS, self._value.encode())

        def onWriteRequest(self, data, offset, withoutResponse, callback):
            if len(data) != 3:
                print("Invalid data received")
                callback(Characteristic.RESULT_UNLIKELY_ERROR)
                return

            houndred_digit = data[
                0] * 100  # data[0] contains the 100 value digit
            remaining_digits = data[1]  # data[1] contains the 10s and 1 values
            angle = houndred_digit + remaining_digits
            strength = data[2]
            self._value = {"angle": angle, "strength": strength}
            #print(f"Write request received for driving control: '{self._value}, byte[0]:{data[0]}, byte[1]:{data[1]}, byte[2]:{data[2]}'")
            self._parent.bluetooth_callback(angle, strength)
            callback(Characteristic.RESULT_SUCCESS)

    def on_state_change(self, state):
        if state == "poweredOn":
            self.bleno.startAdvertising(
                "RaspberryPiZero", ["12345678-1234-5678-1234-56789abcdef0"])
        else:
            self.bleno.stopAdvertising()

    def on_advertising_start(self, error):
        if not error:
            self.bleno.setServices([
                BlenoPrimaryService({
                    "uuid":
                    "12345678-1234-5678-1234-56789abcdef0",
                    "characteristics": [self.driving_control_characteristic]
                })
            ])

    def on_disconnect(self, client_address):
        print("Disconnected from Bluetooth device")
        # Calls the callback function with appropriate arguments to stop the robot
        self.bluetooth_callback(0, 0)

    def run(self):
        self.bleno.start()

    def stop(self):
        self.bleno.stopAdvertising()
        self.bleno.disconnect()


if __name__ == "__main__":
    bt_controller = BluetoothController()

    print("Starting Bluetooth controller... Press Ctrl-C to stop.")
    try:
        bt_controller.run()
        while True:
            pass
    except KeyboardInterrupt:
        print("\nStopping Bluetooth controller...")
    finally:
        bt_controller.stop()
        print("Bluetooth controller stopped.")
