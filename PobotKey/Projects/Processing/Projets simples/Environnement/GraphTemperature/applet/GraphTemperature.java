import processing.core.*; 
import processing.xml.*; 

import eeml.*; 
import processing.serial.*; 
import java.util.Date; 

import java.applet.*; 
import java.awt.*; 
import java.awt.image.*; 
import java.awt.event.*; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class GraphTemperature extends PApplet {







Serial port;

DataOut dOut;

PImage b;
PFont font;
PFont fontb;

// constantes

int gX = 24;
int gY = 34;

int gheight = 210;
int gwidth = 560;

// variables

int nb_mesures = 0;

int time_count = 0;
int time_scale = 1;

int min_temp = 0;
int max_temp = 0;
int ratio = 2;

int[] temperatures = new int[300];
int[] minutes = new int[300];

int clock_min;
int clock_hour;
int clock_date;
int clock_month;
int clock_year;

public void setup() {
  //
  size(600,300);  
  b = loadImage("history.png");
  background(b);  
  //
  font = loadFont("ArialMT-12.vlw"); 
  fontb = loadFont("Arial-BoldMT-12.vlw");
  textFont(font,12);  
  fill(50);
  text("v0.9",5,height-5);
  //  
  println(Serial.list());
  port = new Serial(this,Serial.list()[2],9600);
  // set up DataOut object; requires URL of the EEML you are updating, and your Pachube API key   
  dOut = new DataOut(this, "http://www.pachube.com/api/1267.xml", "6bdd53f70381ad2c09b99b8c9fdded79e73f06f6127091daea715be2ae686f62");   
  //  and add and tag a datastream    
  dOut.addData(0,"temperature");
}

public void draw() {
  // affichage de la temp\u00e9rature en fonction de l'absisse de la souris
  cleanScreen();
  afficherEchelles();
  afficherCourbe();
  afficherClock();
  // changer le curseur et rajouter l'information utile
  if (mouseX >= 24 && mouseX <= 587 && mouseY >= 34 && mouseY <= 244)
  {   
    //
    noCursor(); 
    textAlign(LEFT);
    fill(51,204,102);
    textFont(fontb,12);  
    text(nfc((float)(244-mouseY)*(max_temp-min_temp)/210+min_temp,1),30,50);     
    stroke(191,0,0);
    strokeWeight(1.0f);
    line(mouseX,34,mouseX,244);
    line(24,mouseY,587,mouseY);
  } 
  else {
    cursor(ARROW); 
  }
}

public void cleanScreen() {
  background(b);  
  fill(50);  
  textAlign(LEFT); 
  textFont(font,12);  
  text("v0.9",5,height-5);
}

public void mousePressed() {
  println("("+mouseX+","+mouseY+")"); 

}

public void keyPressed() {
  switch (key) {
  case 'p':
    // print screen
    String filename = "temperature_"+year()+nf(month(),2)+nf(day(),2)+"_"+nf(hour(),2)+nf(minute(),2)+nf(second(),2)+".png";
    save(filename);
    println("> saved image "+filename);
    break;
  case 'r':
    // DISABLED FOR SECURITY
    println("> reinit memory");
    break;
  case 'c':
    updateClock();
    break;
  case 'u':   
    updateMesures();
    updateMinutes();
    updateMinMax();
    updateRatio();
    updateTimeScale();
    updateClock();
    updatePachube();
    //
    println("> "+nb_mesures+" valeurs entre "+min_temp+" et "+max_temp+" degr\u00e9s");
    break;
  default:
    println("> unrecognized command: "+key);
  }
}

public void updatePachube() {
  // updates stream 0 with the value of myVariable.
  dOut.update(0, temperatures[nb_mesures-1]); 
  //  
  if (dOut.updatePachube() == 200){
    // data was updated successfully.
    println("> Pachube update OK.");
  } 
  else {
    println("> Error in Pachube update."); 
  }
}

public void updateClock() {
  port.write("c");
  delay(200);
  while (port.available() > 0) {
    clock_hour = port.read();
    clock_min = port.read();
    clock_date = port.read();
    clock_month = port.read();
    clock_year = 2009;
  }  
}

public void afficherClock() {
  textAlign(LEFT); 
  textFont(font,12);  
  text(nf(clock_hour,2)+"h"+nf(clock_min,2)+" "+nf(clock_date,2)+"/"+nf(clock_month,2)+"/"+clock_year,40,height-5);
}

public void updateTimeScale() {
  // TODO after RTC implementation

  // pour le moment, on compte le nombre de valeurs
  // et on multiplie par 10 minutes

  // la m\u00e9moire peut contenir 255 valeurs = 255 * 10 minutes
  time_count = nb_mesures * 10;

  // l'\u00e9chelle minimum est 24 heures soit valeur = 1440 minutes  
  if (time_count < 1440) {
    time_count = 1440;
  }

  time_scale = time_count/60/12;  

}

public void updateMinMax() {
  // remise en route de l'algorithme par des valeurs min/max impossibles
  min_temp = 500;
  max_temp = 0;
  //
  for (int i = 0; i< nb_mesures; i++)
  { 
    if (temperatures[i] < min_temp) 
    {
      min_temp = temperatures[i];      
    }
    if (temperatures[i] > max_temp)
    {
      max_temp = temperatures[i];      
    }    
  }  
  // normalisation des valeurs
  if (min_temp > max_temp) 
  {
    // d\u00e9tection du cas particulier "pas de valeurs"
    min_temp = 0;
    max_temp = 100;
  } 
  else {
    // arrondir aux 5 pr\u00e9c\u00e9dents et 5 suivants
    min_temp = (min_temp / 5) * 5;
    max_temp = (max_temp / 5) * 5 + 5;
  }
}

public void updateRatio()
{
  ratio = height/(max_temp-min_temp) - 1;

}

public void updateMesures() {
  //
  int i = 0;
  //
  println("update");
  port.write("l");
  println("lecture");
  delay(1000);
  //
  nb_mesures = port.read();
  //
  while (port.available() > 0)
  {
    temperatures[i++] = port.read();
  }  
}

public void updateMinutes() {
  // Premi\u00e8re version, sans 
  for (int i = 1; i <= nb_mesures; i++)
  {
    minutes[nb_mesures-i] = (i-1)*10;

  }

}

public void afficherEchelles() {     
  // \u00e9chelle verticale / axe des ordonn\u00e9es : temp\u00e9rature
  fill(50);  
  textAlign(RIGHT);   
  textFont(font,12);  
  text(min_temp+"\u00b0",20,244);
  text(max_temp+"\u00b0",20,34);
  // \u00e9chelle horizontale / axe des absisces : temps
  fill(50);
  textAlign(CENTER);  
  text("24h",24+gwidth-1*(gwidth*2/time_scale),260); 
  text("48h",24+gwidth-2*(gwidth*2/time_scale),260);
  text("3d",24+gwidth-3*(gwidth*2/time_scale),260);
  text("4d",24+gwidth-4*(gwidth*2/time_scale),260);
  if (time_scale < 5)
  {
    text("12h",24+gwidth-0.5f*(gwidth*2/time_scale),260); 
    text("36h",24+gwidth-1.5f*(gwidth*2/time_scale),260); 
  }  
  if (time_scale == 2)
  {
    text("6h",24+gwidth-0.25f*(gwidth*2/time_scale),260);
    text("18h",24+gwidth-0.75f*(gwidth*2/time_scale),260);
  }
}

public void afficherCourbe() {
  int oldX = 0;
  int oldY = 0;
  // 
  for (int i = 0; i < nb_mesures; i++)
  { 
    int curX = 584-564*minutes[i]/(time_scale*12*60);
    int curY = 244-210*(temperatures[i]-min_temp)/(max_temp-min_temp);
    //
    // println("> mesure n\u00b0 "+i+": "+temperatures[i]+"\u00b0 \u00e0 t="+minutes[i]+" min.");
    //
    if (oldX == 0) { // or oldY equals 0 but one is sufficient
      //       
    } 
    else {
      //
      smooth();
      stroke(51,204,102);
      strokeWeight(5.0f);
      strokeJoin(ROUND);
      line(oldX,oldY,curX,curY);
    }
    //
    oldX = curX;
    oldY = curY;
  }
}

























  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#d4d0c8", "GraphTemperature" });
  }
}
