const express = require('express');
const fs = require('fs'); 

var DECODED_IMAGE_PATH = "./src/decodedImages/xxx.jpg"

module.exports = function ({
    db,
    googleApi
}) { 
    
    const router = express.Router();


    router.get('/', function (request, response) {    
        db.fetchImages(function(err, images){
            if (err) { 
                response.status(500).end();
            } else { 
                response.status(200).json({images});
            }
        });
    });

    router.get('/:id', function (request, response){
        const imageId = request.params.id
        db.fetchImage(imageId, function (err, imageData){ 
            if (err) { 
                response.status(500);
            } else { 
                response.status(200).json({imageData});
            }
        });
    });

    router.get('/position/:positionId', function (request, response){
        const positionId = request.params.positionId;
        db.fetchImageByPositionId(positionId, function (error, image){
            if (error) {
                response.status(500).end();
            } else {
                response.status(200).json(image);
            }
        });
    });

    router.post('/', function (request, response){
        const requestBody = request.body;
        const imageModel = {
            encodedImage: requestBody.encodedImage,
            position: {
                x: requestBody.x,
                y: requestBody.y
            }
        };

        classifyImage(imageModel.encodedImage, function (error, imageDescription) {
            if (error) {
                response.status(400).json({ error: "Wrong image encoding. Could not peroform image detection!" });
            } else {
                imageModel['image_classification'] = imageDescription;
                db.storeImage(imageModel, function (error) {
                    if (error) {
                        response.status(500).end();
                    } else {
                        response.status(201).end();
                        deleteImagePath();
                    }
                });
            }
        });
    });

    
    

    function classifyImage(encodedImage, callback) {
        const buffer = Buffer.from(encodedImage, 'base64');
        fs.writeFile(DECODED_IMAGE_PATH, buffer, function (error) {
            if (error) {
                callback(error, null);
            } else {
                googleApi.performImageDetection(DECODED_IMAGE_PATH, function (error, imageDesciption) {
                    if (error) {
                        callback(error, null);
                    } else {
                        callback(null, imageDesciption);                        
                    }
                });
            }
        });
    }

    function deleteImagePath() {
        fs.unlink(DECODED_IMAGE_PATH, function (error) {
            if (error) {
                console.error('Error:', error);
            } else {
                console.log('File removed successfully');
            }
        });
    }
    
    return router;
}