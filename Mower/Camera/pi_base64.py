import os
import time
import io
from picamera import PiCamera
import base64
from PIL import Image

def take_picture_and_compress(quality=75):
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

def encode_image_to_base64(image_stream):
    encoded_image = base64.b64encode(image_stream.read())
    return encoded_image

def save_base64_string(encoded_image, output_path):
    with open(output_path, 'wb') as output_file:
        output_file.write(encoded_image)

def main():
    base64_folder = "piBase64Strings"
    
    if not os.path.exists(base64_folder):
        os.makedirs(base64_folder)

    print("Type 'p' and press 'Enter' to take a picture or 'q' to quit.")
    
    while True:
        user_input = input()
        
        if user_input == 'p':
            timestamp = time.strftime("%Y%m%d-%H%M%S")
            compressed_image_stream = take_picture_and_compress(quality=75)
            encoded_image = encode_image_to_base64(compressed_image_stream)
            base64_path = os.path.join(base64_folder, f"base64_{timestamp}.txt")
            save_base64_string(encoded_image, base64_path)
            print(f"Encoded image saved to {base64_path}")
        elif user_input == 'q':
            break

if __name__ == "__main__":
    main()
