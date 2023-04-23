const express = require('express'); 

module.exports = function ({
    db
}) { 
    const router = express.Router();


    router.get('/', function (request, response) { 
        db.fetchImages(function(err, images){
            if (err) { 
                response.status(500)
            } else { 
                response.status(201).json({images})
            }
        })
    })

    router.get('/:id', function (request, response){
        
        const imageId = request.params.id
        console.log('erek;asjdkljasd')
        db.fetchImage(imageId, function (err, imageData){ 
            if (err) { 
                response.status(500)
            } else { 
                response.status(200).json({imageData})
            }
        })
    })

    router.post('/', function (request, response){
        const encodedImage = request.body.encodedImage
        db.storeImage(encodedImage, function (err, imageId){
            if (err) { 
                response.status(500)
            } else { 
                response.status(201).json({imageId})
            }
        })
        
    })


    return router;
}