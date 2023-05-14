const express = require('express');
const app = express();
const vision = require('@google-cloud/vision');
// Creates a client
const client = new vision.ImageAnnotatorClient({
    keyFilename: './api-key.json'
});

// Performs label detection on the image file
client
    .labelDetection('./mushu.jpg')
    .then(results => {
        const labels = results[0].labelAnnotations;

        console.log('The image is: ' + labels[0].description);
        //console.log(results);
    })
    .catch(err => {
        console.error('ERROR:', err);
    });


/*const { Storage } = require('@google-cloud/storage');
const vision = require('@google-cloud/vision');

const apiKey = require('../../../API/api-key.json').apiKey;
const client = new vision.ImageAnnotatorClient({
    keyFilename: apiKey,
});

const projectId = 'ims-group-3';
const storage = new Storage({
    projectId
});

async function authenticateImplicitWithAdc() {
    // Listage des buckets de stockage
    const [buckets] = await storage.getBuckets();
    console.log('Buckets:');
    for (const bucket of buckets) {
        console.log(`- ${bucket.name}`);
    }
    console.log('Listed all storage buckets.');

    // Appel de la fonction recognizeImage ici
    try {
        await recognizeImage();
    } catch (error) {
        console.error('Error recognizing image:', error);
    }

    // Autres opérations que vous souhaitez effectuer
}

async function recognizeImage() {
    const [result] = await client.labelDetection('./mushu.jpg');

    const labels = result.labelAnnotations;
    console.log('Labels:');
    labels.forEach(label => console.log(label.description));
}

authenticateImplicitWithAdc();
recognizeImage().catch(console.error);*/
