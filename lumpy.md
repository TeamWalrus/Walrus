---
layout: page
title: Lumpy
permalink: /lumpy/
---

# Adding Bluetooth support to the [Bishop Fox RFID Tastic Thief](https://www.bishopfox.com/resources/tools/rfid-hacking/attack-tools/) for $3.47

Lumpy is based on the previous work done by Fran Brown and the awesome team who worked on the Tastic RFID Thief project over at Bishop Fox. Lumpy improves on [Bishop Fox's Tastic RFID Thief](https://www.bishopfox.com/resources/tools/rfid-hacking/attack-tools/) by adding bluetooth support at a very minimal cost. This enables wireless communication between the long range card reader and our Walrus app, allowing cloned cards to be sent to your Walrus wallet via a bluetooth connection.

![lumpy]({{ "/assets/lumpy_hc06.png" }})

---
## Equipment Needed
You will need a wireless serial Bluetooth RF transceiver. The HC06 is a very cheap solution that does the trick. I picked up one from [ebay](https://www.ebay.com/itm/Wireless-Serial-4-Pin-Bluetooth-RF-Transceiver-Module-HC-06-RS232-With-backplane-/200924726178) for $3.47 USD:


## Change the Baud rate of HC06
First change the operating board rate of the HC06. This will vary on the equipment you have, but there are a few instructions and guides on how to change the default settings of the HC06 using AT commands. Commands vary on the board so here are instructions used for both [HC05](http://www.instructables.com/id/AT-command-mode-of-HC-05-Bluetooth-module/) and [HC06](http://www.instructables.com/id/How-to-Change-the-Name-of-HC-06-Bluetooth-Module/) modules. I found that HC05 commands worked when changing the baud rate of my HC06 board -  ¯\\\_(ツ)\_/¯. So don't be afraid to experiment...

The operating baud rate of the HC06 module needs to be set to `57600` to correctly receive data from the Tastic RFID Thief Arduino board.

The baud rate was found the source code for the Arduino board used in the Bishop fox Tastic RFID Thief project still available for download [here](http://www.bishopfox.com/download/814/). The relevant section of code is shown below:

```csharp
// Set up function from Tastic_RFID_Adrudion
void setup()
{
  pinMode(13, OUTPUT);  // LED
  pinMode(2, INPUT);     // DATA0 (INT0)
  pinMode(3, INPUT);     // DATA1 (INT1)
  
  Serial.begin(57600);   // This is the baud rate we need to configure the HC06 bluetooth module to 
  Serial.println("RFID Readers");
  ...
```

## RX to TX
Simply connect the D1/TX from the Tastic RFID Thief Arduino board to the RX of the HC06 Bluetooth module. The breadboard layout below should give an idea of how the HC06 module should be connected:

![Tastic-modifications-breadboard-layout]({{ "/assets/Tastic-Custom_RFID_Stealer_PCB_du_2018.png" }})

## Power
You also need to steal some power from somewhere. Again the Tastic RFID Thief Arduino board has a spare 5v line which will do. However, you will find that this will draw too much electricity from the Arduino. To solve this problem, provide external power to the Arduino module. Either use a power bank or siphon some juice from the battery pack added by bishop fox (We have also upgraded this battery pack solution - instead of AAA batteries we use Lithium Ion batteries).
