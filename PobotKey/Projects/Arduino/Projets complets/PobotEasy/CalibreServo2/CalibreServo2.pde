/**
 * Calibrage des servos de propulsion d'un Pobot Easy
 *
 * On donne une vitesse à une roue
 * et on affiche le nombre d'incréments observés pour un temps fixe
 *
 * @author Julien Holtzer - julien.holtzer@gmail.com
 * @date 23-05-2009
 *
 */

#include <Servo.h>

// servo object representing the motor
Servo serv;

// connexion du servo (digital)
int servPin = 9;

// connexion de l'odométrie (0 = digital 2, 1 = digital 3)
int odoPin = 0;

// vitesse par défaut
int valeur = 90;

// compteur des incréments
int counter = 0;

void setup()
{
  // liaison série
  Serial.begin(38400);
  
  // interrupt pour le compteur  
  attachInterrupt(odoPin, tick, CHANGE);
  
}

void tick()
{
 counter++; 
}

void loop()
{
  // si on a reçu une nouvelle consigne de vitesse
  if (Serial.available())
  {
    // lecture de la valeur
    valeur = Serial.read();  
  }
  
  // nouvelle boucle : test de la vitesse donnée
  
  // 1) arrêter le servo   
  serv.detach();
  delay(300);
  // 2) réinitialiser le compteur 
  counter = 0;
  // 3) redémarrer le servo à  la vitesse souhaitée
  serv.attach(servPin);
  serv.write(valeur);
  // 4) attendre le délai de mesure
  delay(3000);
  // 5) arrêter le servo
  serv.detach();
  // 6) envoyer les valeurs
  Serial.print(valeur);
  Serial.print(",");
  Serial.print(counter);
  Serial.println();
  
  // attendre 
  delay(500); 
  
}

