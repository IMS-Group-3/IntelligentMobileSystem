const express = require('express');
const app = express();
const vision = require('@google-cloud/vision');

const performLabelDetection = async () => {
    const client = new vision.ImageAnnotatorClient({
        keyFilename: './api-key.json'
    });

    try {
        const [results] = await client.labelDetection('./mushu.jpg');
        const labels = results.labelAnnotations;
        console.log('The image is: ' + labels[0].description);
    } catch (err) {
        console.error('ERROR:', err);
    }
};

performLabelDetection();
