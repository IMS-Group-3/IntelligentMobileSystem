#include <MeAuriga.h>
#include <CircularBuffer.h>

CircularBuffer<int,33> buffer;

MeEncoderOnBoard Encoder_1(SLOT1);
MeEncoderOnBoard Encoder_2(SLOT2);
MeUltrasonicSensor ultraSensor(PORT_7);
MeRGBLed rgbled_0(0,12);

//Used To enable Visual Debugging of mower
bool LIGHT_DEBUG = true;

//flag for Off state
bool IS_ON;

//flag for use in Outside function
bool BeenInside;

//Varible used in the State Machine expecting values M = Manual, O = Outside, I = Inside
char mode;

// Varible used in the timing check
unsigned long ENDTIME = 0;

bool will_Change(int pwmRight, int pwmLeft){
  if (Encoder_1.getCurPwm() != pwmRight || Encoder_2.getCurPwm() != pwmLeft){
    return true;
  }
  else{
    return false;
  }
}

//Set All RGBLeds To The Color Determined by the Values Red Green Blue
void RGBLight(int red,int green,int blue){
  rgbled_0.setColor(0,red,green,blue);
  rgbled_0.show();
}

//Runs Engine Encoder Loops
void RunEncoderLoops(){
  for(int i = 0; i < 10; i++){
    Encoder_1.loop();
    Encoder_2.loop();
    delay(50);
  }
}

/*performs Avoidens routine Curently Reverces for 1 sec 
and then turns left or Right for 0,5 then forward*/
void Avoid(void){
  //Clear Buffer
  buffer.clear();

  //Stopp
  buffer.push(500);
  buffer.push(0);
  buffer.push(0);

  //Take Picture
  buffer.push(-1);

  //Drive Backward for 1 sec
  buffer.push(1000);
  buffer.push(128);
  buffer.push(-128);
  
  //stopp
  buffer.push(500);
  buffer.push(0);
  buffer.push(0);

  if (random(2) == 0){
    //Rotate Right
    buffer.push(500);
    buffer.push(128);
    buffer.push(128);
  }
  else{
    //Rotate Left
    buffer.push(500);
    buffer.push(-128);
    buffer.push(-128);
  }

  //Stoop
  buffer.push(500);
  buffer.push(0);
  buffer.push(0);

  //Forward at Half Speed
  buffer.push(500);
  buffer.push(-128);
  buffer.push(128);
}

//Movment Control Sets Engiens PWMs
void SetEnginenPWM(signed int pwmRight,signed int pwmLeft){
  if(will_Change(pwmRight,pwmLeft)){
    if(LIGHT_DEBUG){
    RGBLight(0,128,0);//set Color Green
    }
    Encoder_1.setTarPWM(pwmRight);//Right Engine negative
    Encoder_2.setTarPWM(pwmLeft);//Left Engine
    RunEncoderLoops();
  }
}

//Interupt service rutine for encoder 1
void isr_process_encoder1(void){
  if(digitalRead(Encoder_1.getPortB()) == 0){
    Encoder_1.pulsePosMinus();
  }
  else{
    Encoder_1.pulsePosPlus();;
  }
}

//Interupt service rutine for encoder 2
void isr_process_encoder2(void){
  if(digitalRead(Encoder_2.getPortB()) == 0){
    Encoder_2.pulsePosMinus();
  }
  else{
    Encoder_2.pulsePosPlus();
  }
}

void setup(){   // put your setup code here, to run once:
  //Setingup Engien Encoder Interupts
  attachInterrupt(Encoder_1.getIntNum(), isr_process_encoder1, RISING);
  attachInterrupt(Encoder_2.getIntNum(), isr_process_encoder2, RISING);
  
  //Start of Seral Comunication
  Serial.begin(115200);

  // Set up leds
  rgbled_0.setpin(44);
  rgbled_0.fillPixelsBak(0, 2, 1);
  
  //seed the random generator
  randomSeed(ultraSensor.distanceCm());
  
  //Set Mode To Off
  mode = ' ';

  //sets off varible
  IS_ON = false;

  //set outside varible
  BeenInside = true;

  //Set PWM 8KHz
  TCCR1A = _BV(WGM10);
  TCCR1B = _BV(CS11) | _BV(WGM12);

  TCCR2A = _BV(WGM21) | _BV(WGM20);
  TCCR2B = _BV(CS21);
  
  // Set current time
  ENDTIME = millis();
}

void loop(){    // put your main code here, to run repeatedly:
  
  if (Serial.available() > 0 && (Serial.peek() == 'I' || Serial.peek() == 'O' || Serial.peek() == 'M' || Serial.peek() == ' ')){
    if(LIGHT_DEBUG){
      RGBLight(128,0,0);//Show Color Color Red
    }
    buffer.clear();
    mode = Serial.read();
  }
  else{
    while (Serial.available() > 0){
      Serial.read();
    }
  }

  switch(mode){
    case 'I':
      if(LIGHT_DEBUG){
        RGBLight(0,0,128);//Show Color Color Blue
      }
      
      if (!BeenInside){
        BeenInside = true;
      }

      if(!IS_ON){
        IS_ON = true;
      }

      if(ENDTIME <= millis() && buffer.size() > 0){
        if(buffer.first() < 0){
          buffer.shift();
          Serial.println("Collision");
        }
        else{
          if(buffer.size() >= 3){
            int Time = buffer.shift();
            int pwmRight = buffer.shift();
            int pwmLeft = buffer.shift();

            SetEnginenPWM(pwmRight,pwmLeft);
            ENDTIME = millis() + Time;
          }
        }
      }
      if(ultraSensor.distanceCm() > 20){ //Check if Distance forward is grater then 20 cm
        if(buffer.size() < 1){
          //Drive Forward
          buffer.push(1000);
          buffer.push(-128);
          buffer.push(128);
        }
        }
      else{ //Distance Less Then 20 cm
        if(LIGHT_DEBUG){
          RGBLight(255,255,255);//Set Color White
        }
        if(buffer.size() <= 0){
          Avoid();//Avoidens rutine
        }
      }
      //Ultra Sonic Sensor Minimum Delay of 100 Millisec
      delay(100);
    break;

    case 'O':
      if(!IS_ON){
        IS_ON = true;
      }

      if(LIGHT_DEBUG){
        RGBLight(0,128,128);//Set Color Cyan
      }

      if (BeenInside){
        if(will_Change(128,-128)){ //Check if pwm will change
          SetEnginenPWM(128,-128);//reverce
        }
        else{
          SetEnginenPWM(-128,128);//forward
        }
        
        if (random(2) == 0){
          //Rotate Right
          buffer.push(500);
          buffer.push(128);
          buffer.push(128);
        }
        else{
          //Rotate Left
          buffer.push(500);
          buffer.push(-128);
          buffer.push(-128);
        }
        BeenInside = false;
      }
    break;

    case 'M':
      if(!IS_ON){
        IS_ON = true;
      }

      if(LIGHT_DEBUG){
        RGBLight(128,0,128);//Show Color purpule
      }

      if(Serial.available() > 0 && Serial.peek() != 'I' && Serial.peek() != 'O' && Serial.peek() != ' '){//Checks if The Serial recive Buffer contains data

        //Convert Recived data To signed ints
        signed int pwmRight = Serial.parseInt();//Read in a Int from the Recive Buffer
        signed int pwmLeft = Serial.parseInt();//Read in a Int from the Recive Buffer
        
        //Checks if pwmRight are in allowed span
        if(pwmRight > 510){
          pwmRight = 510;
        }else if(pwmRight < 0){
          pwmRight = 0;
        }
        //Checks if pwmLeft are in allowed span
        if(pwmLeft > 510){
          pwmLeft = 510;
        }else if(pwmLeft < 0){
          pwmLeft = 0;
        }
        
        //Convert To allowed value Span of (-255) - (255)
        pwmRight = pwmRight - 255;
        pwmLeft = pwmLeft - 255;
        
        //Execute Recived Movment Command
        SetEnginenPWM(pwmRight,pwmLeft);
      }
    break;

    case ' ':
      if(LIGHT_DEBUG){
      RGBLight(128,0,0);//Set Color Red
      }
      if (IS_ON){
        SetEnginenPWM(0,0);
        IS_ON = false;
      }  
    break;

    default:
    if(LIGHT_DEBUG){
    RGBLight(128,128,0);//Set Color yellow
    }
    break;
  }
  
  while (Serial.available() > 1){
    Serial.read();
  }
}