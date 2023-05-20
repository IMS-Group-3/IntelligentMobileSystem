const express = require('express');
const bodyParser = require('body-parser');
const cookieParser = require('cookie-parser');

const swaggerJsdoc = require('swagger-jsdoc');
const swaggerUi = require('swagger-ui-express');
const path = require('./endpoints/path');

module.exports = function (
    {
        image,
<<<<<<< HEAD
=======
        path
>>>>>>> main
    }
) {

    const options = {
        apis: [],
        definition: {
            openapi: '3.0.0',
            info: {
                title: 'Back-end API Documentation',
                version: '1.0.0',
                description: 'API Documentation for the Back-End of the IMS Group 3 project',
                servers: [
                    {
                        url: 'http://localhost:5000/'
                    }
                ]
            },
            tags: [
                {
                    name: 'Positions',
                    description: 'Routes for reading and writing positions',
                },
                {
                    name: 'Images',
                    description: 'Routes for reading and writing image data',
                },
            ],
            paths: {
                '/positions': {
                    get: {
                        tags: ['Positions'],
                        summary: 'Get Positions',
                        description: 'Get all positions',
                        responses: {
                            200: {
                                description: 'Successful operation',
                                content: {
                                    'application/json': {
                                        schema: {
                                            type: 'array',
                                            items: {
                                                type: 'object',
                                                properties: {
                                                    id: { type: 'integer' },
                                                    lat: { type: 'number' },
                                                    lon: { type: 'number' },
                                                    timestamp: { type: 'string' },
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            500: {
                                description: 'Server error',
                            }
                        }
                    },
                    post: {
                        tags: ['Positions'],
                        summary: 'Add Position',
                        description: 'Add a new position',
                        requestBody: {
                            required: true,
                            content: {
                                'application/json': {
                                    schema: {
                                        type: 'object',
                                        properties: {
                                            lat: { type: 'number', description: 'Latitude' },
                                            lon: { type: 'number', description: 'Longitude' },
                                            timestamp: { type: 'string', format: 'date-time', description: 'Timestamp' },
                                        },
                                        required: ['lat', 'lon', 'timestamp'],
                                    }
                                }
                            }
                        },
                        responses: {
                            201: {
                                description: 'Successful operation',
                                content: {
                                    'application/json': {
                                        schema: {
                                            type: 'object',
                                            properties: {
                                                id: { type: 'integer' },
                                                lat: { type: 'number' },
                                                lon: { type: 'number' },
                                                timestamp: { type: 'string' },
                                            }
                                        }
                                    }
                                }
                            },
                            400: {
                                description: 'Invalid data provided',
                            },
                            500: {
                                description: 'Server error',
                            }
                        }
                    }
                },
                '/images': {
                    get: {
                        tags: ['Images'],
                        summary: 'Get Image Data',
                        description: 'Get image data for all images',
                        responses: {
                            200: {
                                description: 'Successful operation',
                                content: {
                                    'application/json': {
                                        schema: {
                                            type: 'array',
                                            items: {
                                                type: 'object',
                                                properties: {
                                                    id: { type: 'integer' },
                                                    filename: { type: 'string' },
                                                    data: { type: 'string' },
                                                    timestamp: { type: 'string' },
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            500: {
                                description: 'Server error',
                            }
                        }
                    },
                    post: {
                        tags: ['Images'],
                        summary: 'Add Image Data',
                        description: 'Add new image data',
                        requestBody: {
                            required: true,
                            content: {
                                'application/json': {
                                    schema: {
                                        type: 'object',
                                        properties: {
                                            image: {
                                                type: 'string'
                                            }
                                        },
                                        required: ['image']
                                    }
                                }
                            }
                        },
                        responses: {
                            200: {
                                description: 'Successful operation'
                            },
                            400: {
                                description: 'Invalid input',
                                content: {
                                    'application/json': {
                                        schema: {
                                            type: 'object',
                                            properties: {
                                                error: {
                                                    type: 'string'
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            500: {
                                description: 'Server error'
                            }
                        }
                    }
                }
            }
        }
    };


    const app = express();

    const specs = swaggerJsdoc(options);
    app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(specs));

    app.use(bodyParser.json({
        limit: '5gb'
    }));
    app.use(cookieParser());

    app.use('/image', image);
    app.use('/paths', path)

    return app;

}