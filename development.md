---
layout: page
title: Development
permalink: /development/
---

## Licensing

Walrus is developed by Daniel Underhay and Matthew Daley (a.k.a. [Team Walrus](mailto:team@project-walrus.io)!) and is [open source](https://github.com/megabug/ProjectWalrus), released under the [GNU General Public License v3.0](https://github.com/megabug/ProjectWalrus/blob/master/LICENSE).

## Contributions

We welcome all kinds of contributions and bug reports, big or small! Development takes place at our [GitHub repository](https://github.com/megabug/ProjectWalrus). There you can file issues (both bugs and enhancement requests) and submit pull requests.

During the initial development of Walrus, changes to the codebase are likely to be frequent and wide-ranging, so if you want to work on a feature, it's wise to reach out first to ensure that your hard work won't be soon obsoleted. After our first full release we hope to gain stability and bring in some of the additional resources expected of a project today, such as a proper test suite and continuous integration.

One area we'd love your help with is contributing translations! If you think you can help us out translating our [Android string resources](https://github.com/megabug/ProjectWalrus/blob/master/app/src/main/res/values/strings.xml) to another language, please get in touch!

## Roadmap

You can always look at our [GitHub issues listing](https://github.com/megabug/ProjectWalrus/issues) for a list of upcoming fixes and changes; particularly those labeled [enhancement](https://github.com/megabug/ProjectWalrus/issues?q=is%3Aissue+is%3Aopen+label%3Aenhancement). Here's a summary of some of the larger ones:

- **Support for more devices!** This is an obvious thing to be working on, so much so that it has its own [label](https://github.com/megabug/ProjectWalrus/issues?q=is%3Aissue+is%3Aopen+label%3Adevice-support). Devices in our sights are:

  - The [MagSpoof](https://samy.pl/magspoof/).

  - The [ESP-RFID-Tool](https://github.com/rfidtool/ESP-RFID-Tool).

  - The various low-cost magstripe readers that use USB serial.

  - Arduino-controlled Wiegand devices over Bluetooth, e.g. our [Lumpy](/lumpy/) design.

- **Support for more card types!** The Proxmark3 and Chameleon Mini already support more cards than exposed by Walrus. Adding this support is just a simple matter of extending the device-specific driver code to handle more card types. Thanks to Walrus's extendible design, this should be an easy one (but don't all developers say things like that?)

- **Tagging of cards**.

- **Location-aware brute-forcing**. Stored cards should be emulated through a device one by one, ordered in increasing distance between the current location and the location the card was captured. This allows a user to attempt to open a reader in the field given all the cards they've captured.

- **Card sharing**, both to other instances of Walrus and export/import from generic formats (XML, CSV, etc.)

- **A location-based wallet view**, so you can view your stored cards by location of capture instead of date order.

- Got a cool idea that isn't already on our radar? [Tell us!](https://github.com/megabug/ProjectWalrus/issues/new?labels=enhancement)
