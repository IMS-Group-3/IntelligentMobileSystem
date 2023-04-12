#include <MeAuriga.h>


MeEncoderOnBoard Encoder_1(SLOT1);
MeEncoderOnBoard Encoder_2(SLOT2);
MeUltrasonicSensor ultraSensor(PORT_7);
MeRGBLed rgbled_0(0,12);

void _loop(){}

void _delay(float seconds) {
  if(seconds < 0.0){
    seconds = 0.0;
  }
  long endTime = millis() + seconds * 1000;
  while(millis() < endTime) _loop();
}
//Sets All Leds To Color By Values Red, Green, Blue and how long in sec it is to show. Danger busy Waiting
void RGBLightBlink(float delay,int red,int green,int blue){
  RGBLight(red,green,blue);
  _delay(delay);
  RGBLight(0,0,0);
}
//Set all leds To color by values Red ,Green, Blue
void RGBLight(int red,int green,int blue){
  rgbled_0.setColor(0,red,green,blue);
  rgbled_0.show();
}
//performs Avoidens routine
void Avoid(void){
  Runing(1,510,000);
  _delay(0,5);
  Runing(1,510,510);
  _delay(1);
}

void Runing(int riktning,signed int pwmRight,signed int pwmLeft){// Movment Control
  /*
  Serial.print(pwmRight);
  Serial.print(" ");
  Serial.println(pwmLeft);
  */
  

    switch(riktning){
      case 0://Stop
        RGBLightBlink(0.5,128,128,128);//White
        Encoder_1.setTarPWM(pwmRight);//Right Engine negative
        Encoder_2.setTarPWM(pwmLeft);//Left Engine
        break;
      case 1://Move
        RGBLightBlink(0.5,0,128,0);//Green
        Encoder_1.setTarPWM(pwmRight);//Right Engine negative
        Encoder_2.setTarPWM(pwmLeft);//Left Engine
        break;
      case 2://Avoid
        RGBLightBlink(0.5,0,0,128);//Blue

        Avoid();
        
        break;
      default://Default skip
        RGBLightBlink(0.5,255,0,0);//Red
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

void setup(){
  // put your setup code here, to run once:
  attachInterrupt(Encoder_1.getIntNum(), isr_process_encoder1, RISING);
  attachInterrupt(Encoder_2.getIntNum(), isr_process_encoder2, RISING);
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

void loop(){
  // put your main code here, to run repeatedly:

  if(ultraSensor.distanceCm()> 10){
    RGBLight(0,0,0);//Black/Off
    //if (Manualdrive){}
    if(Serial.available()){
      //create Recicing arrays
      char pwmRightIn[4];
      char pwmLeftIn[4];
      //start population of reciving arrays
      for(int i = 0; i < 7; i++){
        char charIn = Serial.read();//Read in byte from reciving buffer
        if(i < 4){// if first 4 caracters
          if(charIn == ',') {//if End Of first message add \0
            pwmRightIn[i] = '\0';
          }
          else{//Else add to first message
            pwmRightIn[i] = charIn;
          }
        }
        else{//else 4 last caracters
          pwmLeftIn[i-4] = charIn;
        }
      }
      //Convert To signed int
      signed int pwmRight = atoi(pwmRightIn);
      signed int pwmLeft = atoi(pwmLeftIn);
      //Convert To allowed values -255 - 255
      pwmRight = pwmRight - 255;
      pwmLeft = pwmLeft - 255;
      /*Debug Text
      Serial.print("Speed Right: ");
      Serial.print(pwmRight);
      Serial.print(" Speed Left: ");
      Serial.println(pwmLeft);
      */
      //Send Values to engine control
      if(pwmLeft == pwmRight && pwmLeft == 0){
        Runing(0,0,0);  
      }
      else{
        Runing(1,pwmRight,pwmLeft);
      }
    }
  }
  else{
    RGBLight(255,0,0);//Red
    
    Runing(0,0,0);
    Runing(2,0,0);
  }
  Encoder_1.loop();
  Encoder_2.loop();

  //Engine Encoders Speed Print
  /*
  Serial.print("Spped 1:");
  Serial.print(Encoder_1.getCurrentSpeed());
  Serial.print(" ,Spped 2:");
  Serial.println(Encoder_2.getCurrentSpeed());
  
  //UltraSonic Distance Print
  Serial.print("Distance : ");
  Serial.print(ultraSensor.distanceCm() );
  Serial.println(" cm");
  */
  //Ultra Sonic Delay
  delay(100);
}