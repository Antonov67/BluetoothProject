char val;
int LED = 13;
String command;
void setup()
{
  Serial.begin(9600);
  pinMode(LED, OUTPUT);
  digitalWrite(LED, LOW);
}
void loop()
{
  if (Serial.available())
  {
    val = Serial.read();


    if (val == '*')
    {
      Serial.println(command + ": " + millis());
      // При символе "1" включаем светодиод
      if (command == "1"){
        digitalWrite(LED, HIGH);
      }
      // При символе "0" выключаем светодиод
      if ( command == "0"){
        digitalWrite(LED, LOW);
      }
      command = "";

    }
    else{
      command += val;
    }


  }
}