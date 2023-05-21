const express = require('express');
const mqtt = require('mqtt');
const bodyParser = require('body-parser');

module.exports = function ({

}) {
    const router = express();

    // MQTT broker configuration
    const brokerOptions = {
        port: 8883,
        username: 'IMS-3',
        password: 'IMS-3-password',
        clientId: 'mqtt-client', // Change this if needed
        protocol: 'mqtts',
        rejectUnauthorized: false // Set to true if your broker has a valid SSL certificate
    };

    // Connect to the MQTT broker
    const client = mqtt.connect('mqtts://d5e73d0d.ala.us-east-1.emqxsl.com', brokerOptions);

    let connected = false;

    // Handle MQTT connection
    client.on('connect', () => {
        console.log('Connected to EMQX broker');
        client.subscribe('mower/control', (err) => {
            if (err) {
                console.error('Error subscribing to topic:', err);
            } else {
                console.log('Subscribed to topic: mower/control');
                connected = true;
            }
        });
    });

    // Handle MQTT message received
    client.on('message', (topic, message) => {
        console.log(`Received message on topic '${topic}': ${message}`);
    });

    // API endpoint to receive commands
    router.post('/', (req, res) => {
        const command = req.body.command;
        if (command === 'manual' || command === 'turn_off' || command === 'autonomous') {
            if (connected) {
                client.publish('mower/control', command, (err) => {
                    if (err) {
                        console.error('Error publishing command:', err);
                        res.status(500).json({ message: 'Error publishing command' });
                    } else {
                        console.log('Command sent:', command);
                        res.status(200).json({ message: `Command sent: ${command}` });
                    }
                });
            } else {
                console.log('Not connected to MQTT broker. Command not sent:', command);
                res.status(500).json({ message: 'Not connected to MQTT broker' });
            }
        } else {
            res.status(400).json({ message: 'Invalid command' });
        }
    });

    
    return router;
}
