const vision = require('@google-cloud/vision');

module.exports = function () {

    return {
        
        async performImageDetection(imagePath, callback) {
            const client = new vision.ImageAnnotatorClient({
                keyFilename: './src/component/api-key.json'
            });

            try {
                const [results] = await client.labelDetection(imagePath);
                const labels = results.labelAnnotations;
                callback(null, labels[0].description);
            } catch (error) {
                callback(error, null);
            }
        }
    }
    
}
