#include <EEPROM.h>
#include <ESP8266WiFi.h>
#include <WiFiClient.h> 
#include <ESP8266WebServer.h>
WiFiServer server(80);
char ssid[30];
char password[30];

int buzzer = 2;

String ssid_Arg;
String password_Arg;
String INDEX_HTML;
String ssidr = "";
String passwordr = "";

String read_string(int l, int p){
  String temp="";
  for (int n = p; n < l+p; ++n){
     if(char(EEPROM.read(n))!=';'){
        if(isWhitespace(char(EEPROM.read(n)))){
          //do nothing
        }else temp += String(char(EEPROM.read(n)));
      
     }else n=l+p;
     
    }
  return temp;
}

void setup() {
  pinMode(buzzer, OUTPUT);
  digitalWrite(buzzer,HIGH);
    INDEX_HTML = "<!DOCTYPE HTML>";  
    INDEX_HTML +="<html>";
    INDEX_HTML +="<head>";
    INDEX_HTML +="<meta content=\"text/html; charset=ISO-8859-1\"";
    INDEX_HTML +=" http-equiv=\"content-type\">";
    INDEX_HTML +="<title>ESP8266 Web Form Demo</title>";
    INDEX_HTML +="</head>";
    INDEX_HTML +="<body>";
    INDEX_HTML +="<h1>ESP8266 Web Form Demo</h1>";
    INDEX_HTML +="<FORM action=\"/submit/test/pswd\" method=\"post\">";
    INDEX_HTML +="<P>";
    INDEX_HTML +=ssid_Arg;
    INDEX_HTML +="<br><label>ssid:&nbsp;</label>";
    INDEX_HTML +="<input size=\"30\" maxlength=\"30\" value=\"" ;
    INDEX_HTML +=ssidr;
    INDEX_HTML += "\" name=\"ssid\">";
    INDEX_HTML +="<br>";
    INDEX_HTML +=password_Arg;
    INDEX_HTML +="<br><label>Password:&nbsp;</label><input size=\"30\" maxlength=\"30\"  value=\"";
    INDEX_HTML +=passwordr;
    INDEX_HTML +=" \"name=\"Password\">";
    INDEX_HTML +="<br>";
    INDEX_HTML +="<INPUT type=\"submit\" value=\"Send\"> <INPUT type=\"reset\">";
    INDEX_HTML +="</P>";
    INDEX_HTML +="</FORM>";
    INDEX_HTML +="</body>";
    INDEX_HTML +="</html>";
	delay(1000);
	Serial.begin(115200);
 Serial.println("fg");
  EEPROM.begin(512);
  String string_Ssid="";
  String string_Password="";
  string_Ssid = read_string(30,0); 
  string_Password= read_string(30,100);
  string_Ssid = "tag:"+string_Ssid;
  if(string_Ssid != ""){
    string_Password.toCharArray(password,30);
    string_Ssid.toCharArray(ssid,30);  
  }
  Serial.println(ssid);
  WiFi.softAP(ssid, password);
	//server.on("/", handleRoot);
  //server.on("/on",handleOn);
  //server.on("/off",handleOff);
  //server.on("/submit",handleSubmit);
	server.begin();
 //server.send(200, "text/html", INDEX_HTML);
}

void handleOff(){
  digitalWrite(buzzer, HIGH);
}
/*void handleOn(){
    for (int i = 0; i < 5; i++){
    digitalWrite(buzzer, LOW);
    delay(100);
    digitalWrite(buzzer, HIGH);
    delay(500);
   // server.send(200, "text/html", "<H1>OK</h1>");
   client.println("HTTP/1.1 200 OK");
    client.println("Content-Type: text/html");
    client.println("");
    client.print("Done");
  }
}*/

void loop() {
  WiFiClient client = server.available();
  if (!client) {
    return;
  }
  
  while(!client.available()){
    delay(1);
  }
  
  String reg = client.readStringUntil('\r');
  String f = reg.substring(5,reg.indexOf(" HTTP"));

  Serial.println(f);
  //client.flush();
  if(f=="on"){
    for (int i = 0; i < 5; i++){
    digitalWrite(buzzer, LOW);
    delay(100);
    digitalWrite(buzzer, HIGH);
    delay(500);
  }
  client.println("HTTP/1.1 200 OK");
  client.println("Content-Type: text/html");
  client.println("");
  client.print("Done");
  return;
  }
  else if(f.indexOf(".ico")==-1){
    String sd = f.substring(0,f.indexOf("/"));
    String pd = f.substring(f.indexOf("/")+1,f.length());
    Serial.println(sd);
    Serial.println(pd);
    write_to_Memory(sd,pd);
    client.println("HTTP/1.1 200 OK");
    client.println("Content-Type: text/html");
    client.println("");
    client.print("Done");
    WiFi.disconnect();
    ESP.restart();
  }

  //client.flush();
 /* client.println("HTTP/1.1 200 OK");
  client.println("Content-Type: text/html");
  client.println("");
  client.print("Led pin is now: ");*/
  
}
/*void handleRoot() {
    server.send(200, "text/html", INDEX_HTML);
}
void handleSubmit(){
 write_to_Memory(String(server.arg("ssid")),String(server.arg("Password")));
  server.send(200, "text/html", INDEX_HTML);
WiFi.disconnect();
ESP.restart();
}*/
void write_to_Memory(String s,String p){
 s+=";";
 write_EEPROM(s,0);
 p+=";";
 write_EEPROM(p,100);
 EEPROM.commit();
}
void write_EEPROM(String x,int pos){
  for(int n=pos;n<x.length()+pos;n++){
     EEPROM.write(n,x[n-pos]);
  }
}
