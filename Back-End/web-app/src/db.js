const sqlite3 = require('sqlite3');
const db = new sqlite3.Database('backend-db.db');

db.run(
    `CREATE TABLE IF NOT EXISTS path (
        pathId INTEGER PRIMARY KEY AUTOINCREMENT, 
        start_time TEXT, 
        end_time TEXT
    )`
);

db.run(
    `CREATE TABLE IF NOT EXISTS position (
        positionId INTEGER PRIMARY KEY AUTOINCREMENT,
        x INTEGER NOT NULL, 
        y INTEGER NOT NULL,
        timestamp TEXT NOT NULL,
        collision_occured BOOLEAN DEFAULT FALSE, 
        pathId INTEGER NOT NULL,
        FOREIGN KEY(pathId) REFERENCES path(pathId)
    )`
);

db.run(
    `CREATE TABLE IF NOT EXISTS image (
        imageId INTEGER PRIMARY KEY AUTOINCREMENT,
        encodedImage TEXT NOT NULL,
        image_classification TEXT,
        positionId INTEGER,
        FOREIGN KEY(positionId) REFERENCES position(positionId) 
    )`
);


module.exports = function ({

}){

    return {
        storeImage (imageModel, callback) {
            fetchPositionId(imageModel.position, function (error, positionId){
                if (error) {
                    callback(error, null);
                } else {
                    const query = `INSERT INTO image (encodedImage, image_classification, positionId) VALUES (?, ?, ?)`;
                    const values = [imageModel.encodedImage, imageModel.image_classification, positionId];
                    db.run(query, values, function (error) {
                        callback(error);
                    });
                }
            });
            
        },

        fetchImages(callback) {
            db.all(
                `SELECT * FROM image`,
                function (err, images) {
                    if (err) {
                        callback(err, null);
                    } else {
                        callback(null, images);
                    }
                }
            );
        },

        fetchImage(imageId, callback) {
            db.get(
                `
                SELECT * FROM image WHERE imageId=?
                `, 
                [imageId], 
                function (err, image){
                    if (err){
                        callback(err, null);
                    } else {
                        callback(null, image);
                    }
                }
            );
        }, 

        fetchImageByPositionId(positionId, callback) {
            const query = `SELECT * FROM image WHERE positionId=?`;
            db.get(query, [positionId], function (error, image){
                callback(error, image);
            });
        },

        fetchPaths(callback) {
            const query = `SELECT * FROM path`;
            db.all(query, function(error, paths){
                callback(error, paths)
            });
        }, 

        fetchPath(pathId, callback) {
            if (pathId == 0) {
                const query1 = `SELECT max(pathId) from path`;
                db.get(query1, function (error, pathId) {
                    if (error) {
                        callback(error, null);
                    } else {
                        const query2 = `SELECT * FROM position WHERE pathId=?`;
                        db.all(query2, [pathId['max(pathId)']], function (error, positions) {
                            callback(error, positions);
                        })
                    }
                });
            } else {
                const query = `
                SELECT * FROM position 
                WHERE pathId = ?
                ORDER BY position.timestamp ASC`;
                const values = [pathId];
                db.all(query, values, function (error, path) {
                    callback(error, path);
                });
            }

        }, 
     
        storePosition(positionModel, callback){
            getLastPathId(positionModel, function (error, paths) {
                if (error) {
                    callback(error);
                } else {
                    if (paths.length === 0) {
                        storePathToDatabase(positionModel.startTime, function (error, pathId) {
                            if(error){
                                callback(error);
                            } else {
                                storePositionToDatabase(positionModel, pathId, function (error){
                                    callback(error);
                                });
                            }
                        });
                    } else {
                        const pathId = paths[0].pathId;
                        storePositionToDatabase(positionModel, pathId, function (error){
                            callback(error);
                        });
                    }
                }
            });
        }

    }//return 
    
    function getLastPathId(positionModel, callback){
        const query = `SELECT pathId FROM path WHERE start_time= ?`;
        const value = [positionModel.startTime];
        db.all(query, value, function (error, paths){
            if (error){
                callback(error, null);
            } else { 
                callback(null, paths);
            }
        });
    }

    function storePathToDatabase(startTime, callback) {
        const query = `INSERT INTO path (start_time) VALUES (?)`;
        const values = [startTime];
        db.run(query, values, function (error) {
            callback(error, this.lastID);
        });
    }

    function storePositionToDatabase(positionModel, pathId, callback){
        const query = `
            INSERT INTO position (x, y, timestamp, collision_occured, pathId)
            VALUES (?,?,?,?,?)`;
        const values = [positionModel.x, positionModel.y, positionModel.timestamp, positionModel.collisionOcurred, pathId];
        db.run(query, values, function (error) {
            callback(error);
        });
    }

    function fetchPositionId(position, callback){
        const query = `SELECT positionId FROM position WHERE x=? AND y=?`;
        const values = [position.x, position.y];
        db.get(query, values, function (error, position){
            if (error){
                callback(error, null);
            } else {
                callback(null, position.positionId);
            }
        });
    }

}
