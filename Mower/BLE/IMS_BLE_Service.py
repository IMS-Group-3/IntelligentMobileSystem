from pybleno import Bleno, BlenoPrimaryService, Characteristic


class DrivingControlCharacteristic(Characteristic):

    def __init__(self):
        Characteristic.__init__(
            self, {
                "uuid": "12345678-1234-5678-1234-56789abcdef2",
                "properties": ["write", "read"],
                "value": None,
            })

    def onWriteRequest(self, data, offset, withoutResponse, callback):
        print("Received driving control data:", data)
        callback(Characteristic.RESULT_SUCCESS)


# Create a Bleno instance
bleno = Bleno()


# Set the Bleno advertising parameters
def onStateChange(state):
    if state == "poweredOn":
        bleno.startAdvertising("RaspberryPiZero",
                               ["12345678-1234-5678-1234-56789abcdef0"])
    else:
        bleno.stopAdvertising()


bleno.on("stateChange", onStateChange)


# Set the GATT server services and characteristics
def onAdvertisingStart(error):
    if not error:
        bleno.setServices([
            BlenoPrimaryService({
                "uuid":
                "12345678-1234-5678-1234-56789abcdef0",
                "characteristics": [DrivingControlCharacteristic()]
            })
        ])


bleno.on("advertisingStart", onAdvertisingStart)

# Start the Bleno service
bleno.start()

print("Press Ctrl-C to stop the application...")

try:
    while True:
        pass
except KeyboardInterrupt:
    print("\nStopping the application...")

finally:
    # Clean up
    bleno.stopAdvertising()
    bleno.disconnect()
    print("Application stopped.")
