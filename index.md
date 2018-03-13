---
layout: home
---

<p align="center"><img src="/assets/walrus.png"></p>

<h1 class="post-title" align="center">Walrus - Card Cloning Made Simple</h1>

{% include play-store-badge.html %}

Walrus enables you to use your existing contactless card cloning devices with your Android device. Using a simple interface, cards can be read into a wallet to be written or emulated later.

Designed for physical security assessors, Walrus has features that will help you in your next red team engagement.

## Demo

As an example, Walrus can be used to tap into the power of the Tastic RFID Thief long range card reader, allowing for walk-by cloning of a victim's access card in a matter of seconds. The cloned card can then quickly be emulated or written to a blank card via an attached Proxmark.

<style>.embed-container { position: relative; padding-bottom: 56.25%; height: 0; overflow: hidden; max-width: 100%; } .embed-container iframe, .embed-container object, .embed-container embed { position: absolute; top: 0; left: 0; width: 100%; height: 100%; }</style><div class='embed-container'><iframe src='https://player.vimeo.com/video/247914436' frameborder='0' webkitAllowFullScreen mozallowfullscreen allowFullScreen></iframe></div>
<p></p>

## Design

Walrus does not implement all of the functionality supported by a given card device and card type pair, instead choosing to implement only the subset that is common across most devices and most useful in real-world usage. This includes basic tasks such as card reading, writing and emulation, as well as some device-specific functionality such as antenna tuning or device configuration.

## Features

### Multi-device Support

Walrus [currently supports](/device-support/) the [Proxmark3](https://github.com/Proxmark/proxmark3) and the [Chameleon Mini](https://github.com/emsec/ChameleonMini), with more device support on the way.

### Bulk Card Reading

Bulk card reading continually reads cards from a device until manually stopped. This lets you easily capture multiple cards as you move around your target location. Vibration feedback lets you know when you have successfully read a card without the need for any interaction with your card reading or Android devices. Simultaneous device I/O support lets you use different devices to read multiple card types all at once or write a card on a device while you are still reading from another device.

### Location Awareness

Each time a card is seen, it is recorded with your location, making it easier to determine where the card may grant access to.

### Brute-forcing (Coming soon)

If you find a card reader that needs to be opened, your stored cards can be emulated through a device one by one. The cards are ordered in increasing distance between the reader and the location the card was captured, increasing the chances of a matching card being emulated and the reader granting access.

### Shareable Cards (Coming soon)

Cards can be shared between instances of Walrus and exported for external use. This can be useful if there are two consultants on an engagement; one consultant can be tasked with cloning an access card from an employee in the field while the second consultant can write or emulate the access card at a reader.

### More to come!

Check out our [GitHub enhancement issues](https://github.com/megabug/Walrus/issues?q=is%3Aissue+is%3Aopen+label%3Aenhancement) to get a sneak peek of what's in the pipeline!

## Ready to start?

Check out the [Getting Started](/docs/getting-started/) page!

## Want to help?

Check out the [Development](/development/) page!
