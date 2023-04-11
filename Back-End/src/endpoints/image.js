const express = require('express'); 

module.exports = function ({

}) { 
    const router = express.Router();

    router.get('/', function (request, response) { 
        response.status(200).json({recieved: true}); 
    })

    return router;
}