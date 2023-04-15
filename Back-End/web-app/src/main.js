const awilix = require('awilix');
const container = awilix.createContainer();

container.register({
    image: awilix.asFunction(require('./endpoints/image.js')),
    app : awilix.asFunction(require('./app.js'))
});

container.resolve('app').listen(8080, function () {
    console.log('Running!')
});
