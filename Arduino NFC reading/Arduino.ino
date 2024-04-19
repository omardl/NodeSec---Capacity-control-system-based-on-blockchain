#include <SPI.h>
#include <MFRC522.h>
#include <SoftwareSerial.h>

#define ledR            1
#define RST_PIN         9          
#define SS_PIN          10       

#include <LiquidCrystal.h>
LiquidCrystal lcd(7, 8, 6, 5, 3 , 4);

MFRC522 mfrc522(SS_PIN, RST_PIN); 
const byte pinBuzzer = 2;

char option = ' ';


void setup() {

  // Buzzer y led rojo para alertas
  pinMode(ledR, OUTPUT);
  pinMode(pinBuzzer, OUTPUT);
  
  digitalWrite(ledR, LOW);
  digitalWrite(pinBuzzer, LOW); 
	
  // Comunicación serial con la Raspberry
  Serial.begin(9600);
	
  // Lector NFC  
  SPI.begin();        
	
  mfrc522.PCD_Init();

  // Pantalla LCD
  lcd.begin(16, 2);
  lcd.setCursor(0,0);
  lcd.write("Bienvenid@! ^_^" );
  lcd.setCursor(0,1);
  lcd.write("Acerca tu NFC!!!");

}



void loop() {
        
  // Lectura NFC
  if ( mfrc522.PICC_IsNewCardPresent()) {  

    if ( mfrc522.PICC_ReadCardSerial()) {        
      
      for (byte i = 0; i < mfrc522.uid.size; i++) {
      
        Serial.print(mfrc522.uid.uidByte[i] < 0x10 ? " 0" : " ");
        Serial.print(mfrc522.uid.uidByte[i], HEX);   
      
      } 
      
      Serial.println("");
      
      // LCD apagado mientras se espera una respuesta
      digitalWrite(ledR, LOW);
      
      delay(1500);
    }      
  }

  digitalWrite(ledR, HIGH);

  // Respuesta de la Raspberry 
  if (Serial.available() > 0) {

    option = Serial.read();

    if (option == '1') {
      // Todo correcto, entrada/salida aceptada
      // Mensaje de bienvenida
      // Led apagado
      // Suena el buzzer para indicar el mensaje (sólo un pitido)

      digitalWrite(ledR, LOW);

      lcd.setCursor(0,0);
      lcd.write("Adelante! ;)    ");
      lcd.setCursor(0,1);
      lcd.write("Ten un buen dia!"); 
            
      option = 'z';

      for (int i = 0; i < 31000; i++) {

        digitalWrite(pinBuzzer, HIGH);    
      
      } 

      digitalWrite(pinBuzzer, LOW); 
      delay(2000);

      lcd.setCursor(0,0);
      lcd.write("Bienvenid@! ^_^ ");
      lcd.setCursor(0,1);
      lcd.write("Acerca tu NFC!!!"); 
        
    } else if (option == '0') {
      // La cámara no detecta a nadie en la entrada/salida
      // Mensaje informativo
      // Sonido del buzzer (tres pitidos seguidos indicando error) y señal del led
      
      digitalWrite(ledR, LOW);

      lcd.setCursor(0,0);
      lcd.write("Acercate mas! No");
      lcd.setCursor(0,1);
      lcd.write("te veo bien O_O "); 

      option = 'z';
          
      digitalWrite(ledR, HIGH); 

      for (int i = 0; i < 20000; i++) {
      
        digitalWrite(pinBuzzer, HIGH);        
      
      }
      
      digitalWrite(pinBuzzer, LOW);
      
      delay(1000);
      
      
      for (int i = 0; i < 20000; i++) {
      
        digitalWrite(pinBuzzer, HIGH);        
      
      }
      
      digitalWrite(pinBuzzer, LOW);
      
      delay(1000); 
      
      
      for (int i = 0; i < 20000; i++) {
      
        digitalWrite(pinBuzzer, HIGH);        
      
      } 
      
      digitalWrite(pinBuzzer, LOW); 
      
      delay(2000);


      lcd.setCursor(0,0);
      lcd.write("Bienvenid@! ^_^ ");
      lcd.setCursor(0,1);
      lcd.write("Acerca tu NFC!!!"); 

    } else if (option == '2') {
      // La cámara detecta a varias personas en la entrada/salida
      // Mensaje informativo
      // Sonido del buzzer (tres pitidos seguidos indicando error) y señal del led
            
      digitalWrite(ledR, LOW);
           
      lcd.setCursor(0,0);
      lcd.write("HEY! Sin colarse");
      lcd.setCursor(0,1);
      lcd.write("De uno en uno!  "); 

      option = 'z';
            
      digitalWrite(ledR, HIGH); 

      for (int i = 0; i < 20000; i++) {

        digitalWrite(pinBuzzer, HIGH);        
      
      }
      
      digitalWrite(pinBuzzer, LOW);
      
      delay(1000);
      
      
      for (int i = 0; i < 20000; i++) {
      
        digitalWrite(pinBuzzer, HIGH);        
      
      }
      
      digitalWrite(pinBuzzer, 0);
      
      delay(1000); 
      
      
      for (int i = 0; i < 20000; i++) {
      
        digitalWrite(pinBuzzer, HIGH);        
      
      } 
      
      digitalWrite(pinBuzzer, LOW); 
      
      delay(2000);


      lcd.setCursor(0,0);
      lcd.write("Bienvenid@! ^_^ ");
      lcd.setCursor(0,1);
      lcd.write("Acerca tu NFC!!!"); 

    } else if (option == '5') {
      // No se reconoce el NFC, su ID no está registrado en la BBDD interna
      // Mensaje informativo
      // Sonido del buzzer (tres pitidos seguidos indicando error) y señal del led

      lcd.setCursor(0,0);
      lcd.write("NFC invalido! No");
      lcd.setCursor(0,1);
      lcd.write(" es una entrada ");
          
      option = 'z';
            
      digitalWrite(ledR, HIGH); 

      for (int i = 0; i < 20000; i++) {
      
        digitalWrite(pinBuzzer, HIGH);        
      
      }
      
      digitalWrite(pinBuzzer, LOW);
      
      delay(1000);
      
      
      for (int i = 0; i < 20000; i++) {
        
        digitalWrite(pinBuzzer, HIGH);        
      
      }
      
      digitalWrite(pinBuzzer, LOW);
      
      delay(1000); 


      for (int i = 0; i < 20000; i++) {

        digitalWrite(pinBuzzer, HIGH);        
      
      } 
      
      digitalWrite(pinBuzzer, LOW); 
      
      delay(2000);

      lcd.setCursor(0,0);
      lcd.write("Bienvenid@! ^_^ ");
      lcd.setCursor(0,1);
      lcd.write("Acerca tu NFC!!!"); 

    } else if (option == '9') {
      // Error critico: error en la 
      // Mensaje informativo (teléfono de ayuda, equipo técnico)
      // Sonido del buzzer y señal del led en parpadeo constante

      lcd.setCursor(0,0);
      lcd.write("Algo va muy mal ");
      lcd.setCursor(0,1);
      lcd.write("Ayuda 272737475 ");       

      for (int i = 0; i < 31000; i++) {
        
        digitalWrite(pinBuzzer, HIGH);
        digitalWrite(ledR, HIGH);  

      } 
            
      digitalWrite(ledR, LOW);
      digitalWrite(pinBuzzer, LOW); 

      while(1) {

        digitalWrite(ledR, HIGH);
        digitalWrite(pinBuzzer, HIGH);

        delay(1000);
        
        digitalWrite(pinBuzzer, LOW);
        digitalWrite(ledR, LOW);

        delay(3000);
      
      }
    }
  }	
}