const express = require('express');
const bodyParser = require('body-parser'); 

module.exports = function (
    {
        image
    }
) { 

    const app = express(); 
    app.use(bodyParser.json({
        limit: "50000kb"
    }));

    app.use('/image', image);

    app.get('/', function (req, res) { 
        res.send('HOME PAGE!')
    })

    app.get('/position', function (req, res) { 
        res.send('HELLO THERE')
    })
    return app; 

}
