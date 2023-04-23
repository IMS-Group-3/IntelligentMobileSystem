const mysql = require('mysql')
const sqlite3 = require('sqlite3')
const db = new sqlite3.Database('backend.db')

  db.run(`
        create table if not exists image(
            imageId INTEGER PRIMARY KEY AUTOINCREMENT,
            encodedImage TEXT
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

        fetchImage(callback) {
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
