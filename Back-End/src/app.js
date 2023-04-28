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

    return app; 

}
