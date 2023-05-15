from pybleno import Bleno, BlenoPrimaryService, Characteristic


class BluetoothController:

    def __init__(self):
        self.driving_control_characteristic = self.DrivingControlCharacteristic(
        )
        self.bleno = Bleno()
        self.bleno.on("stateChange", self.on_state_change)
        self.bleno.on("advertisingStart", self.on_advertising_start)

    class DrivingControlCharacteristic(Characteristic):

        def __init__(self):
            Characteristic.__init__(
                self, {
                    "uuid": "12345678-1234-5678-1234-56789abcdef2",
                    "properties": ["read", "write"],
                    "value": None,
                })
            self._value = ""

        def onReadRequest(self, offset, callback):
            print("Read request received for driving control")
            callback(Characteristic.RESULT_SUCCESS, self._value.encode())

        def onWriteRequest(self, data, offset, withoutResponse, callback):
            if len(data) != 2:
                print("Invalid data received")
                callback(Characteristic.RESULT_UNLIKELY_ERROR)
                return

            angle = data[0]
            strength = data[1]
            self._value = {"angle": angle, "strength": strength}
            #print(f"Write request received for driving control: '{self._value}'")
            callback(Characteristic.RESULT_SUCCESS)

    def get_controller_values(self):
        return self.driving_control_characteristic._value

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
