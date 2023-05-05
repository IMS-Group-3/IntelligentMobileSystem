const express = require('express');
const app = express();
const port = 3000;

app.use(express.json());

app.post('/coordinates', function(req, res) {
  const { x, y } = req.body;

  if (typeof x === 'number' && typeof y === 'number') {
    res.status(200).send({ x, y });
  } else {
    res.status(400).send('Bad request');
  }
});

app.listen(port, function() {
  console.log(`Server listening at http://localhost:${port}`);
});
