import os
import time
from picamera import PiCamera

def take_picture(output_path):
    with PiCamera() as camera:
        camera.resolution = (1024, 768)
        camera.start_preview()
        time.sleep(2)  # Camera warm-up time
        camera.capture(output_path)
        camera.stop_preview()

def main():
    output_folder = "piPictures"
    if not os.path.exists(output_folder):
        os.makedirs(output_folder)

    print("Type 'p' and press 'Enter' to take a picture or 'q' to quit.")
    
    while True:
        user_input = input()
        
        if user_input == 'p':
            timestamp = time.strftime("%Y%m%d-%H%M%S")
            output_path = os.path.join(output_folder, f"pic_{timestamp}.jpg")
            take_picture(output_path)
            print(f"Picture saved to {output_path}")
        elif user_input == 'q':
            break

if __name__ == "__main__":
    main()
