#include <MeAuriga.h>

MeEncoderOnBoard Encoder_1(SLOT1);
MeEncoderOnBoard Encoder_2(SLOT2);
MeUltrasonicSensor ultraSensor(PORT_7);
MeRGBLed rgbled_0(0,12);
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
//Runs Encoder Loops
void EncoderLoop(){
for(int i=0;i<10;i++){
    Encoder_1.loop();
    Encoder_2.loop();
    delay(100);
  }
}

//performs Avoidens routine Curently Reverces for 1 sec and then stops
void Avoid(void){
  //Drive Backward for 0,5 sec
  Runing(1,128,-128);
  EncoderLoop();
  _delay(1);
  //Turn Right for 1 sec
  Runing(0,000,000);
  EncoderLoop();
  _delay(1);
}s
//Movment Control Sets Engiens PWMs And Takes a Driectional Argument as "switchCase" 0 = Stop; 1 = Move; 2 = Avoid
void Runing(int switchCase,signed int pwmRight,signed int pwmLeft){
  switch(switchCase){
    case 0://Stop
      RGBLightBlink(0.5,128,128,128);//Blink Color White
      Encoder_1.setTarPWM(pwmRight);//Right Engine negative
      Encoder_2.setTarPWM(pwmLeft);//Left Engine
      break;
    case 1://Move
      RGBLightBlink(0.5,0,128,0);//Blink Color Green
      Encoder_1.setTarPWM(pwmRight);//Right Engine negative
      Encoder_2.setTarPWM(pwmLeft);//Left Engine
      break;
    case 2://Avoid
      RGBLightBlink(0.5,0,0,128);//Blink Color Blue
      Avoid();//Curently Broken
      break;
    default://Default skip
      RGBLightBlink(0.5,255,0,0);//Blink Color Red
    break;
  }  
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

  //Set PWM 8KHz
  TCCR1A = _BV(WGM10);
  TCCR1B = _BV(CS11) | _BV(WGM12);

  TCCR2A = _BV(WGM21) | _BV(WGM20);
  TCCR2B = _BV(CS21);
}

void loop(){    // put your main code here, to run repeatedly:

  if(ultraSensor.distanceCm()> 10){ //Check if Distance forward is grater then 10 cm
    RGBLight(0,0,0);//Set Color Off
     
    if(Serial.available()){  //Checks if The Serial recive Buffer contains data
      //creating Reciving arrays
      char pwmRightIn[4];
      char pwmLeftIn[4];
      
      //start population of reciving arrays
      for(int i = 0; i < 7; i++){
        char charIn = Serial.read();//Read in byte from the Recive Buffer
        if(i < 4){  // if first 4 caracters
          if(charIn == ',') {  //if End Of first message add \0
            pwmRightIn[i] = '\0';
          }
          else{   //Else add to first message
            pwmRightIn[i] = charIn;
          }
        }
        else{    //else 4 last caracters
          pwmLeftIn[i-4] = charIn;
        }
      }
      
      //Convert Recived data To signed int
      signed int pwmRight = atoi(pwmRightIn);
      signed int pwmLeft = atoi(pwmLeftIn);
      
      //Convert To allowed value Span of (-255) - (255)
      pwmRight = pwmRight - 255;
      pwmLeft = pwmLeft - 255;
      
      //Send Values to engine control
      if(pwmLeft == pwmRight && pwmLeft == 0){
        //Execute Stopp
        Runing(0,0,0);
      }
      else{
        //Execute Recived Movment Command
        Runing(1,pwmRight,pwmLeft);
      }
    }
  }
  else{ //Distance Less Then 10 cm
    RGBLight(255,0,0);//Set Color Red
    
    Runing(2,0,0);
  }

  //Engiens Loop
  Encoder_1.loop();
  Encoder_2.loop();

  //Ultra Sonic Sensor Minimum Delay of 100 Millisec
  delay(100);
}