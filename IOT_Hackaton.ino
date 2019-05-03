#include <sstream>
#include "FirebaseESP8266.h"
#include <ESP8266WiFi.h>

#define FIREBASE_HOST "https://shazzam-87ac5.firebaseio.com"
#define FIREBASE_AUTH "XAsZAETBDryBPiY1IMnOT7Qcb0eIg43KGw1JMOES"

FirebaseData firebaseData;
String pathFbDB = "/shazzam-87ac5";

char *ssid="Hamama";
char *password="ilovehamama";

int buttonPin = 4;
int movmentPin = 14;
int ledPin = 5;

unsigned long currentMillis = 0;  // save the current time;
const unsigned long period = 10000;  // how much time to delay  300000 == 5 seconds 


void setup() {
  Serial.begin(115200);
  initButtonsMod(); // initiate all buttons mods
  connectWifi();  // connect the device to the wifi
  initFireBase();   // initiate the connection to fire base
  
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, HIGH);
}

void loop() {
  bool doorStatus = manageButtonWithCloud(); 

  if(!doorStatus){
    movmetSensor();
  }

  //delay(8000);
}

void movmetSensor(){
  if(millis() < currentMillis + period){   // Check if period have passed
    return;
  }
  
  int val = digitalRead(movmentPin);
  
  if (val == 1){
    setDataInFirebase("intruderDetected", 1);
    currentMillis  = millis();
    Serial.println("movement");
    Serial.println(val);
    std::string text;
    std::stringstream ss;
    ss << "Will now wait " << period << " milliseconds untill next intruder alarm";
    text = ss.str();
    Serial.println(text.c_str());
    
  }
  else{
    Serial.println("not moving");
    Serial.println(val);
  }
}

bool manageButtonWithCloud(){ // This function turn the led from depending on the value on the server
  // Get button state
  //  int buttonState = digitalRead(buttonPin);
  //  Serial.println(buttonState);

  int openDoorValue = getDataFromFirebase("Homes/GuyHome/openDoor");
  Serial.println("OpenDoor:");
  Serial.println(openDoorValue);

  if (openDoorValue) {
    // Open the door
    digitalWrite(ledPin, HIGH);
    
    setDataInFirebase("doorState", 1);
    return true;
  }
  else {
    // Close the door
    digitalWrite(ledPin, LOW);
    
    setDataInFirebase("doorState", 0);
    return false;
  }
}

void connectWifi(){ // connect to the wifi with the ssid and password variables
  Serial.begin(115200);
  delay(10);
  
  // Connect to WAP
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

void initFireBase(){
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);

  String path = "/shazzam-87ac5";
  String jsonStr;
}

void initButtonsMod() {  // set the mod of all the buttons
  pinMode(buttonPin, INPUT);
  pinMode(movmentPin, INPUT);
}

int getDataFromFirebase(String parameterName) {
  int value = -1;
  if (Firebase.getInt(firebaseData, parameterName)){
    if (firebaseData.dataType() == "int"){
      value = firebaseData.intData();
      //Serial.println(value);
    }
  } else {
    Serial.println("not found");
  }

  return value;
}

void setDataInFirebase(String parameterName, int value) {
  char* path = "Homes/GuyHome/";
  
  if (Firebase.setInt(firebaseData, path + parameterName, value))
  {
    Serial.println("PASSED");
    Serial.println("PATH: " + firebaseData.dataPath());
    Serial.println("TYPE: " + firebaseData.dataType());
    Serial.print("VALUE: ");
    if (firebaseData.dataType() == "int")
      Serial.println(firebaseData.intData());
    else if (firebaseData.dataType() == "float")
      Serial.println(firebaseData.floatData(), 5);
    else if (firebaseData.dataType() == "double")
      Serial.println(firebaseData.doubleData(), 9);
    else if (firebaseData.dataType() == "boolean")
      Serial.println(firebaseData.boolData() == 1 ? "true" : "false");
    else if (firebaseData.dataType() == "string")
      Serial.println(firebaseData.stringData());
    else if (firebaseData.dataType() == "json")
      Serial.println(firebaseData.jsonData());
    Serial.println("------------------------------------");
    Serial.println();
  }
  else
  {
    Serial.println("FAILED");
    Serial.println("REASON: " + firebaseData.errorReason());
    Serial.println("------------------------------------");
    Serial.println();
  }
}
