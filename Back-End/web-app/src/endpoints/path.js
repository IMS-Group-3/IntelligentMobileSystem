const express = require('express')
module.exports = function ({
    db
}) { 
    
    const router = express.Router();

    router.post('/path', function (request, response){
        
    })

    router.get('/:pathId', function (request, response) {
        const pathId = request.params.pathId
        response.status(200).json([
            {
                x: 5000,
                y: 5000,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: true
            },
            {
                x: 5100,
                y: 5100,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: false
            },

            {
                x: 5200,
                y: 5200,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: false
            },
            {
                x: 5100,
                y: 5300,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: false
            },
            {
                x: 5000,
                y: 5400,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: false
            },
            {
                x: 4900,
                y: 5000,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: false
            },
            {
                x: 4800,
                y: 5100,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: false
            },
            {
                x: 4700,
                y: 5200,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: true
            },
            {
                x: 4600,
                y: 5300,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: false
            },
            {
                x: 4500,
                y: 5300,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: false
            },
            {
                x: 4400,
                y: 10000,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: false
            },
            {
                x: 4400,
                y: 10500,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: false
            },
            {
                x: 4400,
                y: 10700,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: false
            },
            {
                x: 4400,
                y: 10500,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: true
            },
            {
                x: 4400,
                y: 11000,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: true
            },
            {
                x: 4400,
                y: 13500,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: false
            },
            {
                x: 2000,
                y: 13500,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: false
            },
            {
                x: 2500,
                y: 12500,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: false
            },
            {
                x: 2500,
                y: 11500,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: false
            },
            {
                x: 2500,
                y: 10500,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: true
            },
            {
                x: 2500,
                y: 10000,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: false
            },
            {
                x: 2500,
                y: 9500,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: false
            },
            {
                x: 2500,
                y: 9000,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: false
            },
            {
                x: 2500,
                y: 8500,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: false
            },
            {
                x: 2500,
                y: 8000,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: false
            },
            {
                x: 2500,
                y: 7500,
                timestamp: "2023-04-12 17:30:20",
                collision_occured: true
            }
        ])
    })

    router.get('/', function (request, response) {

        response.status(200).json([
            {
                pathId: 1,
                startTime: "2023-04-12 17:30:20"
            }, 
            {
                pathId: 2,
                startTime: "2023-04-12 17:30:20"
            }, 
            {
                pathId: 3,
                startTime: "2023-04-12 17:30:20"
            }, 
        ])
    })

    return router;
};
