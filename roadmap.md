---
layout: page
title: Roadmap
permalink: /roadmap/
---

You can always look at our [GitHub issues listing](https://github.com/megabug/Walrus/issues) for a list of upcoming fixes and changes; particularly those labeled [enhancement](https://github.com/megabug/Walrus/issues?q=is%3Aissue+is%3Aopen+label%3Aenhancement). Here's a summary of some of the larger ones:

- **Support for more devices!** This is an obvious thing to be working on, so much so that it has its own [label](https://github.com/megabug/Walrus/issues?q=is%3Aissue+is%3Aopen+label%3Adevice-support). Devices in our sights are:

  - The [MagSpoof](https://samy.pl/magspoof/).

  - The [ESP-RFID-Tool](https://github.com/rfidtool/ESP-RFID-Tool).

  - The various low-cost magstripe readers that use USB serial.

  - Arduino-controlled Wiegand devices over Bluetooth, e.g. our [Lumpy](/lumpy/) design.

- **Support for more card types!** The Proxmark3 and Chameleon Mini already support more cards than exposed by Walrus. Adding this support is just a simple matter of extending the device-specific driver code to handle more card types. Thanks to Walrus's extensible codebase design, this should be an easy one (but don't all developers say things like that?)

- **Tagging of cards**.

- **Location-aware brute-forcing**. Stored cards should be emulated through a device one by one, ordered in increasing distance between the current location and the location the card was captured. This allows a user to attempt to open a reader in the field given all the cards they've captured.

- **Card sharing**, both to other instances of Walrus and export/import from generic formats (XML, CSV, etc.)

- **A location-based wallet view**, so you can view your stored cards by location of capture instead of date order.

- Got a cool idea that isn't already on our radar? [Tell us!](https://github.com/megabug/Walrus/issues/new?labels=enhancement)
