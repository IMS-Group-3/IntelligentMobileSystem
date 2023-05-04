const sqlite3 = require('sqlite3')
const db = new sqlite3.Database('backend.db')


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
        }
    }
    
}
