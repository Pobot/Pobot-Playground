/**
 * This source code is release under the MIT License.
 *
 */

#ifndef _EASY_PINCE_H
#define _EASY_PINCE_H

#include "WProgram.h"
#include "Servo.h"

// Values


#define PINCE_OUVERTE 110  
#define PINCE_FERMEE  38

#define BRAS_HAUT  40
#define BRAS_BAS  120


#define pinBras    7
#define pinPince   8

// Class EasyPince
class EasyPince
{

public:

  void ouvrePince(void);
  void fermePince(void);

  boolean isPinceOuverte(void);
  boolean isPinceFermee(void);
  boolean isPinceBloquee(void);

  void levePince(void);
  void baissePince(void);
  void anglePince(int);

  void attachServo(Servo *s1, Servo *s2) {  
    servoBras = s1;
    servoPince = s2;
    servoBras->attach(pinBras);
    servoPince->attach(pinPince);
  }

  void disablePince(void)
  {
    servoBras->detach(); 
    servoPince->detach(); 
  }
  
  void enablePince(void)
  {
    servoBras->attach(pinBras);
    servoPince->attach(pinPince);
  }

  void testPince();

private:
  Servo *servoBras;
  Servo *servoPince;  

};
#endif


