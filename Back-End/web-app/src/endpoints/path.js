const express = require('express');
const cookies = require('cookies');

const SQLITE_CONSTRAINT = 19;

module.exports = function ({
    db
}) { 

    const router = express.Router();

    router.post('/position', function (request, response) {
        const body = request.body;
        const positionModel = {
            startTime: body.startTime,
            x: body.x,
            y: body.y,
            timestamp: body.timestamp,
            collisionOcurred: body.collisionOcurred
        };
        db.storePosition(positionModel, function (error){
            if(error != null){
              response.status(500).end(); 
            } else { 
                response.status(201).end();
            }
        })
    })

    router.get('/', function (request, response) {
        db.fetchPaths(function (error, paths){
            if (error != null) {
                response.status(500).end();
            } else {
                response.status(200).json(paths);
            }
        });
    });

    router.get('/:pathId', function (request, response) {
        const pathId = request.params.pathId;
        db.fetchPath(pathId, function (error, path) {
            if (error != null) { 
                response.status(500).end();
            } else { 
                response.status(200).json(path);
            }
        });
    });

    

    return router;
};