import paho.mqtt.client as mqtt
import time
import json

broker_address = "o59cc8ec.ala.us-east-1.emqxsl.com"
port_number = 8883
username = "IMS-3"
password = "IMS-3-password"
client_number = "1"


def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("Connected successfully to the broker.")
        client.subscribe(
            "mower/control")  # Subscribe to the mower/control topic
    else:
        print(f"Connection failed with result code {rc}")


def on_disconnect(client, userdata, rc):
    if rc != 0:
        print("Unexpected disconnection.")
    else:
        print("Disconnected from the broker.")


def on_message(client, userdata, msg):
    payload = msg.payload.decode()
    print("received message: {}".format(payload))
    try:
        data = json.loads(payload)
        command = data.get("command")
        if command == "turn_off":
            #TODO Turn off the mower
            print("Mower turned off")
    except json.JSONDecodeError as e:
        print(f"Error decoding JSON: {e}")


client = mqtt.Client(client_number)
client.username_pw_set(username, password)
client.on_connect = on_connect
client.on_message = on_message
client.on_disconnect = on_disconnect
client.tls_set()  # configure TLS settings
client.loop_start()  # Start the MQTT client loop (non blocking)
client.connect(broker_address, port_number, 60)  # Connect to the MQTT broker

try:
    while True:
        time.sleep(1)
except KeyboardInterrupt:
    print("Exiting...")

client.loop_stop()  # Stop the MQTT client loop
client.disconnect()  # Disconnect from the broker
print("Mqtt script closed.")
