const axios = require('axios');

const endpoint = 'http://localhost:3000/coordinates';

axios
    .post(endpoint, { x: 1.5, y: 2.5 })
    .then((response) => {
        console.log(response.data);
    })
    .catch((error) => {
        console.log(error.response.data);
    });

axios
    .post(endpoint, { x: 'not a number', y: 2.5 })
    .then((response) => {
        console.log(response.data);
    })
    .catch((error) => {
        console.log(error.response.data);
    });

axios
    .post(endpoint, { x: 1.5, y: 'not a number' })
    .then((response) => {
        console.log(response.data);
    })
    .catch((error) => {
        console.log(error.response.data);
    });