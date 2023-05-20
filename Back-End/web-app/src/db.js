const sqlite3 = require('sqlite3')
const db = new sqlite3.Database('backend-db.db')

var START_TIME = "";

db.run(
    `CREATE TABLE IF NOT EXISTS path (
        pathId INTEGER PRIMARY KEY AUTOINCREMENT, 
        start_time TEXT, 
        end_time TEXT
    )`
)

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
)

db.run(
    `CREATE TABLE IF NOT EXISTS image (
        imageId INTEGER PRIMARY KEY AUTOINCREMENT,
        encodedImage TEXT NOT NULL,
        image_classification TEXT,
        positionId INTEGER,
        FOREIGN KEY(positionId) REFERENCES position(positionId) 
    )`
)


module.exports = function ({

}){

    function storeNewPath(startTime) {
        const query = `INSERT INTO path (start_time) VALUES (?)`;
        const values = [startTime];
        db.run(query, values, function (error, success) {
            if (error != null) {
                return error
            } else {
                return success
            }
        })
    }
    return {
        storeImage (image, callback) {
            db.run(
                `INSERT INTO image (encodedImage) VALUES (?)`,
                [image],
                function (err) {
                    if (err) {
                        callback(err, null)
                    } else {
                        callback(null, this.lastID)
                    }
                }
            )
        },

        fetchImages(callback) {
            db.all(
                `select * from image`,
                function (err, images) {
                    if (err) {
                        callback(err, null)
                    } else {
                        callback(null, images)
                    }
                }
            )
        },

        fetchImage(imageId, callback) {
            db.get(
                `
                SELECT * FROM image WHERE imageId=?
                `, 
                [imageId], 
                function (err, image){
                    if (err){
                        callback(err, null)
                    } else { 
                        console.log(image)
                        callback(null, image)
                    }
                }
            )
        }, 

        fetchPaths(callback) {
            const query = `SELECT * FROM path`;
            db.all(query, function(error, paths){
                callback(error, paths)
            });
        }, 

        fetchPath(pathId, callback) {
            const query = `
                SELECT * FROM position 
                WHERE pathId = ?
                ORDER BY position.timestamp ASC`;
            const values = [pathId];
            db.all(query, values, function (error, path) {
                callback(error, path);
            });
        }, 

        storePositions(positionModel, callback) {
            if(positionModel.isNewPath == true) {
                storePath(positionModel.startTime, function (error, pathId) {
                    callback(error, pathId, null)
                });
            } else {
                const query = `
                        INSERT INTO position (x, y, timestamp, collision_occured, pathId)
                        VALUES (?,?,?,?,?)`;
                const values = [positionModel.x, positionModel.y, positionModel.datetime, positionModel.collisionOcurred, positionModel.pathId]
                db.run(query, values, function (error) {
                    if (error != null) {
                        callback(error, null, null);
                    } else {
                        callback(null, positionModel.pathId, this.lastID);
                    }
                });
            }
        }

    }//return 
    
    function storePath(startTime, callback) {
        const query = `INSERT INTO path (start_time) VALUES (?)`;
        const values = [startTime];
        db.run(query, values, function (error) {
            callback(error, this.lastID);
        })
    }

}
