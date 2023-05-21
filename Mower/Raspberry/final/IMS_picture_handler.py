import os
import time
import io
from picamera import PiCamera
import base64
from PIL import Image


class IMS_picture_handler():

    def save_picture(self):
        base64_folder = "piBase64Strings"
        if not os.path.exists(base64_folder):
            os.makedirs(base64_folder)
        timestamp = time.strftime("%Y%m%d-%H%M%S")
        compressed_image_stream = self.take_picture_and_compress(quality=75)
        encoded_image = self.encode_image_to_base64(compressed_image_stream)
        base64_path = os.path.join(base64_folder, f"base64_{timestamp}.txt")
        self.save_base64_string(encoded_image, base64_path)

    def send_picture(self, compressed_image, x_coor, y_coor):
        # Send the encoded image to the server as raw JSON
        base64_image = self.encode_image_to_base64(compressed_image)
        url = 'http://16.16.68.202/image'  #Webserver URL for HTTP request
        data = {'encodedImage': base64_image, 'x': x_coor, 'y': y_coor}
        json_data = json.dumps(
            data)  # Convert the data dictionary to a JSON string
        headers = {
            'Content-Type': 'application/json; charset=utf-8',
            'Content-Length': str(len(json_data.encode('utf-8')))
        }
        response = requests.post(url, data=json_data, headers=headers)

        if response.status_code == 201:
            #print("Image uploaded successfully.")
            #image_id = response.json()['imageId']
            return True
        else:
            print(
                f"Error uploading the image. Error code: {response.status_code}"
            )
            return False

    def take_picture_and_compress(self, quality=74):
        stream = io.BytesIO()
        with PiCamera() as camera:
            camera.resolution = (1024, 768)
            camera.start_preview()
            time.sleep(2)  # Camera warm-up time
            camera.capture(stream, format='jpeg')
            camera.stop_preview()

        stream.seek(0)
        image = Image.open(stream)
        compressed_stream = io.BytesIO()
        image.save(compressed_stream, "JPEG", quality=quality)
        compressed_stream.seek(0)
        return compressed_stream

    def encode_image_to_base64(self, image_stream):
        encoded_image = base64.b64encode(image_stream.read())
        return encoded_image.decode("utf-8")

    def save_base64_string(self, encoded_image, output_path):
        with open(output_path, 'wb') as output_file:
            output_file.write(encoded_image)


IMS_pic = IMS_picture_handler()

#IMS_pic.send_picture(IMS_pic.take_picture_and_compress)
