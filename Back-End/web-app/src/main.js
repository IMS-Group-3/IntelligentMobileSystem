const awilix = require('awilix');
const container = awilix.createContainer();

container.register({
    image: awilix.asFunction(require('./endpoints/image.js')),
    app : awilix.asFunction(require('./app.js')), 
    db: awilix.asFunction(require('./db.js'))
});

container.resolve('app').listen(5000, function () {
    console.log('Running!')
});
