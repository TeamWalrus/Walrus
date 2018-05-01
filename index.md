---
layout: home
---

<p align="center"><img src="/assets/walrus.png"></p>

<h1 class="post-title" align="center">Walrus</h1>
<h2 align="center">{{ site.description }}</h2>

{% include play-store-badge.html %}

Walrus is an Android app that enables you to use contactless card cloning devices with your Android device. Using a simple interface in the style of Google Pay, cards can be read into a wallet to be written or emulated later.

Designed for physical security assessors, Walrus has features that will help you in your next red team engagement.

Walrus is [open source](https://github.com/megabug/Walrus), released under the [GNU General Public License v3.0](https://github.com/megabug/Walrus/blob/master/LICENSE).

## Demo

As an example, Walrus can be used to tap into the power of the Tastic RFID Thief long range card reader, allowing for walk-by cloning of a victim's access card in a matter of seconds. The cloned card can then quickly be emulated or written to a blank card via an attached Proxmark.

<style>.embed-container { position: relative; padding-bottom: 56.25%; height: 0; overflow: hidden; max-width: 100%; } .embed-container iframe, .embed-container object, .embed-container embed { position: absolute; top: 0; left: 0; width: 100%; height: 100%; }</style><div class='embed-container'><iframe src='https://player.vimeo.com/video/247914436' frameborder='0' webkitAllowFullScreen mozallowfullscreen allowFullScreen></iframe></div>
<p></p>

## Design

Walrus is designed with red teaming in mind and hence implements common device functionality that is most useful in real-world usage. This includes basic tasks such as card reading, writing and emulation, as well as some device-specific functionality such as antenna tuning or device configuration.

## Features

### Multi-device support

Walrus [currently supports](/device-support/) the [Proxmark3](https://github.com/Proxmark/proxmark3) and the [Chameleon Mini](https://github.com/emsec/ChameleonMini), with more device support on the way.

### Bulk card reading

Bulk card reading continually reads cards from a device until manually stopped, letting you easily capture multiple cards as you move around your target location. Vibration feedback lets you know when you have successfully read a card without the need for any interaction with your card or Android devices. Simultaneous device operation support lets you use different devices at the same time. This can let you read multiple card types all at once, or write a card on a device while you are still reading from another device.

### Location awareness

Each time a card is seen, it is recorded with your location, making it easier to determine where the card may grant access to.

### Brute-forcing (coming soon)

If you find a physical control (e.g. door or turnstile) with a card reader that needs to be passed, the cards stored in your wallet can be emulated through a device one by one. The cards are ordered in increasing distance between the reader and the location the card was captured, increasing the chances of a matching card being emulated quicker and the reader granting access.

### Shareable cards (coming soon)

Cards can be shared between instances of Walrus and exported for external use. This can be useful if there are two testers on an engagement; one tester can be tasked with cloning an access card from an employee in the field while the second tester can write or emulate the access card at a reader.

### More to come!

Check out our [roadmap](/roadmap/) to get a sneak peek of what's in the pipeline!

## Ready to start?

Check out the [Getting Started](/docs/getting-started/) page!

## Want to help?

Check out our [GitHub repository](https://github.com/megabug/Walrus)!
