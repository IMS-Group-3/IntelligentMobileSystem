const express = require('express');
const bodyParser = require('body-parser'); 

module.exports = function (
    {
        image
    }
) { 

    const app = express(); 
    app.use(bodyParser.json());

    app.use('/image', image);

    app.get('/', function (req, res) { 
        res.status(200).json({name: "alaa"})
    })
    return app; 

}
