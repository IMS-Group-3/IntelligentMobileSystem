import paho.mqtt.client as mqtt
import time
import json

broker_address = "d5e73d0d.ala.us-east-1.emqxsl.com"
port_number = 8883
username = "IMS-3"
password = "IMS-3-password"
client_number = "1"


class IMS_MQTT_listener():

    def __init__(self, callback):
        self.callback = callback

    def on_connect(self, client, userdata, flags, rc):
        if rc == 0:
            print("Connected successfully to the broker.")
            self.client.subscribe(
                "mower/control")  # Subscribe to the mower/control topic
        else:
            print(f"Connection failed with result code {rc}")

    def on_disconnect(self, client, userdata, rc):
        if rc != 0:
            print("Unexpected disconnection.")
        else:
            print("Disconnected from the broker.")

    def on_message(self, client, userdata, msg):
        payload = msg.payload.decode()
        print("received message: {}".format(payload))
        self.callback(payload)

    def run(self):
        self.client = mqtt.Client(client_number)
        self.client.username_pw_set(username, password)
        self.client.on_connect = self.on_connect
        self.client.on_message = self.on_message
        self.client.on_disconnect = self.on_disconnect
        self.client.tls_set()  # configure TLS settings
        self.client.loop_start()  # Start the MQTT client loop (non blocking)
        self.client.connect(broker_address, port_number,
                            60)  # Connect to the MQTT broker
        try:
            while True:
                time.sleep(1)
        except KeyboardInterrupt:
            print("Exiting...")
            self.client.loop_stop()  # Stop the MQTT client loop
            self.client.disconnect()  # Disconnect from the broker
        print("Mqtt script closed.")
