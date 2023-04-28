#include <MeAuriga.h>

MeEncoderOnBoard Encoder_1(SLOT1);
MeEncoderOnBoard Encoder_2(SLOT2);
MeUltrasonicSensor ultraSensor(PORT_7);
MeRGBLed rgbled_0(0,12);

bool LIGHT_DEBUG = true;
//State Enum decleration
enum Mode{INSIDE, OUTSIDE, MANUAL}mode;

//Empty Loop used by _delay. Danger! Busy Waiting
void _loop(){}

//Waits the amount of time specified in seconds. Danger! Busy Waiting
void _delay(float seconds) {
  if(seconds < 0.0){
    seconds = 0.0;
  }
  long endTime = millis() + seconds * 1000;
  while(millis() < endTime) _loop();
}

/*Set All RGBLeds To The Color Determined by the Values Red Green Blue
and how long in sec it is to show. Danger! Busy Waiting*/
void RGBLightBlink(float onTime,int red,int green,int blue){
  RGBLight(red,green,blue);//Set Light
  _delay(onTime);
  RGBLight(0,0,0);//Set Light Off
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
    delay(100);
  }
}

//performs Avoidens routine Curently Reverces for 1 sec and then stops
void Avoid(void){
  //Stopp
  SetEnginenPWM(000,000);
  RunEncoderLoops();
  _delay(0.5);

  //Todo Comunicate to the raspberry pi that a picture is to be taken.

  //Drive Backward for 1 sec
  SetEnginenPWM(128,-128);
  RunEncoderLoops();
  _delay(1);
  
  //stopp
  SetEnginenPWM(000,000);
  RunEncoderLoops();
  _delay(0.5);

  //Rotate Right
  SetEnginenPWM(128,128);
  RunEncoderLoops();
  _delay(0.5);

  //Stoop
  SetEnginenPWM(000,000);
  RunEncoderLoops();
  _delay(0.5);

  //Forward at Half Speed
  SetEnginenPWM(-128,128);
  RunEncoderLoops();
}

//Movment Control Sets Engiens PWMs
void SetEnginenPWM(signed int pwmRight,signed int pwmLeft){

  if(LIGHT_DEBUG){
    RGBLightBlink(0.5,0,128,0);//Blink Color Green
  }

  Encoder_1.setTarPWM(pwmRight);//Right Engine negative
  Encoder_2.setTarPWM(pwmLeft);//Left Engine
}

void isr_process_encoder1(void)
{
  if(digitalRead(Encoder_1.getPortB()) == 0)
  {
    Encoder_1.pulsePosMinus();
  }
  else
  {
    Encoder_1.pulsePosPlus();;
  }
}

void isr_process_encoder2(void)
{
  if(digitalRead(Encoder_2.getPortB()) == 0)
  {
    Encoder_2.pulsePosMinus();
  }
  else
  {
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
  
  //Set Mode To Manual
  mode = MANUAL;

  //Set PWM 8KHz
  TCCR1A = _BV(WGM10);
  TCCR1B = _BV(CS11) | _BV(WGM12);

  TCCR2A = _BV(WGM21) | _BV(WGM20);
  TCCR2B = _BV(CS21);
}

void loop(){    // put your main code here, to run repeatedly:

  switch(mode){
    case INSIDE:
      if(ultraSensor.distanceCm()> 20){ //Check if Distance forward is grater then 20 cm
        if(LIGHT_DEBUG){
          RGBLightBlink(0.5,0,0,128);//Blink Color Blue
        }

        //Drive Forward
        SetEnginenPWM(-255,255);
        RunEncoderLoops();
        _delay(1);
      }
      else{ //Distance Less Then 20 cm
        if(LIGHT_DEBUG){
          RGBLight(255,0,0);//Set Color Red
        }
        
        Avoid();//Avoidens rutine
      }

      //Ultra Sonic Sensor Minimum Delay of 100 Millisec
      delay(100);
    break;

    case OUTSIDE:
      SetEnginenPWM(128,-128);
      RunEncoderLoops();
    break;

    case MANUAL:
      if(LIGHT_DEBUG){
        RGBLightBlink(0.5,128,0,128);//Blink Color purpule
      }
      if(Serial.available()){  //Checks if The Serial recive Buffer contains data
        
        if(LIGHT_DEBUG){
          RGBLightBlink(0.5,0,128,128);//Blink Color turquse
        }

        //Convert Recived data To signed ints
        signed int pwmRight = Serial.parseInt();//Read in a Int from the Recive Buffer
        signed int pwmLeft = Serial.parseInt();//Read in a Int from the Recive Buffer
        
        Serial.print("pwmRight : ");
        Serial.println(pwmRight);
        Serial.print("pwmLeft : ");
        Serial.println(pwmLeft);
        
        //Checks if pwmRight are in allowed span
        if(pwmRight>510){
          pwmRight = 510;
        }else if(pwmRight<0){
          pwmRight = 0;
        }
        //Checks if pwmLeft are in allowed span
        if(pwmLeft > 510){
          pwmLeft = 510;
        }else if(pwmLeft < 0){
          pwmLeft = 0;
        }
        
        Serial.print("pwmRight : ");
        Serial.println(pwmRight);
        Serial.print("pwmLeft : ");
        Serial.println(pwmLeft);
        
        //Convert To allowed value Span of (-255) - (255)
        pwmRight = pwmRight - 255;
        pwmLeft = pwmLeft - 255;
        
        Serial.print("pwmRight : ");
        Serial.println(pwmRight);
        Serial.print("pwmLeft : ");
        Serial.println(pwmLeft);

        //Execute Recived Movment Command
        SetEnginenPWM(pwmRight,pwmLeft);
        RunEncoderLoops();

        if(LIGHT_DEBUG){
          RGBLightBlink(0.5,128,128,128);//Blink Color white
        }
      }
    break;

    default:
    if(LIGHT_DEBUG){
    RGBLightBlink(0.5,128,128,0);//Blink Color yellow
    }
    break;
  }
  
}