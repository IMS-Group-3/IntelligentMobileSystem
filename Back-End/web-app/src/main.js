const awilix = require('awilix');
const container = awilix.createContainer();

container.register({
    image: awilix.asFunction(require('./endpoints/image.js')),
    app : awilix.asFunction(require('./app.js')), 
    db: awilix.asFunction(require('./db.js')), 
    path: awilix.asFunction(require('./endpoints/path.js')),
    googleApi: awilix.asFunction(require('./component/google-api.js'))
});

container.resolve('app').listen(8080, function () {
    console.log('OPERATING!')
});
