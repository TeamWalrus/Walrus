---
layout: home
---

![Logo]({{ "/assets/walrus.png" }})
# Project Walrus - Card Cloning Made Simple

Walrus enables you to use your existing contactless card cloning devices with your Android device. Using a simple interface, cards can be read into a wallet to be written or emulated later.

Designed for physical security assessors, Walrus has features that will help you in your next red team engagement. Bulk card reading lets you easily capture multiple cards as you move around your target location, while simultaneous device reading lets you use different devices to read multiple card types all at once.

Walrus currently supports the [Proxmark3](https://github.com/Proxmark/proxmark3) and the [Chameleon Mini](https://github.com/emsec/ChameleonMini), with more device support on the way.

<style>.embed-container { position: relative; padding-bottom: 56.25%; height: 0; overflow: hidden; max-width: 100%; } .embed-container iframe, .embed-container object, .embed-container embed { position: absolute; top: 0; left: 0; width: 100%; height: 100%; }</style><div class='embed-container'><iframe src='https://player.vimeo.com/video/247914436' frameborder='0' webkitAllowFullScreen mozallowfullscreen allowFullScreen></iframe></div>
---

# Features

## Device Agnostic
Walrus is device agnostic, meaning you can use a wide range of card cloning devices. Walrus currently supports the Proxmark3 and Chameleon Mini Rev.G and partial support for the Tastic RFID Thief. We are further developing Walrus to add support for new devices such as the [BLE-Key](http://hackerwarehouse.com/product/blekey/), [MagSpoof v2](https://store.ryscc.com/collections/all/products/magspoof) and [RFID-Tool](https://github.com/rfidtool/ESP-RFID-Tool/blob/master/README.md#esp-rfid-tool).

## 'Walk by' Cloning
Walrus can tap into the power of the Tastic RFID Thief long range card reader and allows for 'walk by' cloning of a victim's access card in a matter of seconds. The cloned card can then be emulated or written to a new blank card via an attached Proxmark. Instructions on adding Bluetooth support to your Tastic RFID Thief for $3.47 can be found [here](/lumpy/).

## LF/HF Tune and card cloning with the Proxmark 3
Gone are the days of `hw tune` :)

![Proxmark3-Screenshot]({{ "/assets/prox_tune1.png" }})

## Bypass the 8 card slot limit of the Chameleon Mini Rev.G.
Your Walrus wallet size is limited only by the amount of free storage on your device.
By default, Walrus will emulate cards on card slot 1 of the attached Chameleon Mini device. However, this can be adjusted in the device settings:

![ChameleonMini-Screenshot]({{ "/assets/chameleon_1.png" }})


## Share cards (coming soon)
In some situations, there might be two consultants on an engagement. One consultant can be tasked with cloning an access card from an employee using the RFID Tastic Thief. Once an access card has been obtained, the cloned card can be sent to the second consultant, who is waiting close by and is ready to write a new card or emulate the access card on a Proxmark.
