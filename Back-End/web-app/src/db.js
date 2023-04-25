const mysql = require('mysql')
const sqlite3 = require('sqlite3')
const db = new sqlite3.Database('backend.db')


db.run(`
      create table if not exists position(
          positionId INTEGER PRIMARY KEY AUTOINCREMENT,
          x INT,
          y INT,
          collision_occured boolean,
          timestamp TIMESTAMP NOT NULL,
          FOREIGN KEY(pathId) REFERENCES path(pathId)
      )
`)


db.run(`
      create table if not exists image(
          imageId INTEGER PRIMARY KEY AUTOINCREMENT,
          imageData TEXT,
          image_classification TEXT,
          FOREIGN KEY(positionId) REFERENCES position(positionId)
      )
`)


db.run(`
      create table if not exists path(
          pathId INTEGER PRIMARY KEY AUTOINCREMENT,
          start TIMESTAMP NOT NULL
      )
`)


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
