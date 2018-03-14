---
layout: page
title: Getting Started
permalink: /docs/getting-started/
---

* Contents
{:toc}

## Installation

You can get Walrus from the Google Play Store:

{% include play-store-badge.html %}

Alternatively, if you're comfortable with doing so, you can clone the [Git repository](https://github.com/megabug/Walrus) and build Walrus yourself. Check out the [README](https://github.com/megabug/Walrus/blob/master/README.md) file for build instructions.

## Connect your device

Start Walrus and you'll be presented with your new, blank wallet:

<img src="/assets/blank-wallet.png" class="screenshot sized-screenshot">

(You may be asked to allow Walrus access to your location. This is use to add location information to each card you read; it's necessary to start listening for location updates ASAP in order to ensure an accurate fix is ready by the time a card is ready to read.)

Connect your Proxmark3 or Chameleon Mini to your Android device. You may require a [OTG USB](https://www.androidpit.com/usb-otg-what-it-means-and-how-to-use-it) cable or adapter.

You may be asked to grant Walrus permissions to access the device:

<img src="/assets/device-perms.png" class="screenshot sized-screenshot">

Upon successful connection, you'll see a short message:

<img src="/assets/device-connected.png" class="screenshot sized-screenshot">

## Check your device

Check that Walrus has correctly identified your device. Go to the device list by selecting the device list toolbar button:

<img src="/assets/device-list-icon.png" class="screenshot" width="40">

<p class="arrow">↓</p>

<img src="/assets/device-list.png" class="screenshot sized-screenshot">

Select your newly attached device and you should see a screen showing your device's version information:

<img src="/assets/device-info.png" class="screenshot sized-screenshot">

At this point, you should understand the capabilities of your device that Walrus (currently) supports. Check the [device support](/device-support/) page to see what your device can do. While most devices can read cards, only some can write or emulate cards back out again.

## Read a card

You're now set to add a new card and read its card data. First, note that Walrus distinguishes between cards and card data. Cards have metadata like a human readable name, date of creation, notes, and so on, *as well as* card data. Card data is the actual ID or other token held on a physical access control card that is read by a card reader.

From the wallet, select the "*Add New Card*" action:

<img src="/assets/add-new-card-button.png" class="screenshot sized-screenshot">

A screen with a new card appears. Here, you can set the card's name and add notes as necessary:

<img src="/assets/new-card.png" class="screenshot sized-screenshot">

Next, select the "*Read Card*" button. You should see a dialog pop up indicating that card data is ready to be read:

<img src="/assets/reading-card.png" class="screenshot sized-screenshot">

Place a physical card on your device's antenna. If all goes well, you should be returned to the card view with the newly read card data and your current location (shown here zoomed out!):

<img src="/assets/physical-card-on-antenna.jpg" class="screenshot" style="max-height: 400px">

<img src="/assets/card-read.png" class="screenshot sized-screenshot">

After saving your card into your wallet by selecting the save icon, you'll return to your wallet with the new card showing:

<img src="/assets/save-icon.png" class="screenshot" width="40">

<p class="arrow">↓</p>

<img src="/assets/wallet-with-read-card.png" class="screenshot sized-screenshot">

## Writing a card

If your device supports it, writing a card is simple.

Select the card in the wallet to be taken to its detailed information screen:

<img src="/assets/view-card.png" class="screenshot sized-screenshot">

Place a writable physical card on your device's antenna and select the "*Write Card*" button. A dialog will pop up indicating that data is being written:

<img src="/assets/writing-card.png" class="screenshot sized-screenshot">

After this, the card data should be written.

## Emulating a card

Again, if your device supports it, emulating a card is easy, being much the same as writing a card (see above).

As before, select the card in the wallet to be taken to its detailed information screen. This time, select the "*Emulate Card*" button. Currently, as Walrus only supports the Chameleon Mini for emulation, the card data is sent to the device and emulation is started immediately. (When support for other devices that don't permanently store a card in order to emulate it is added, a dialog will pop up much the same as when writing a card.)

You can now hold your device's antenna up to a reader and the card data will be read.

<!-- <img src="/assets/TODO" class="screenshot sized-screenshot"> -->

## Bulk reading cards

Bulk reading cards lets you stealthy read multiple cards over a period of time without having to interact with your Android device.

From the wallet, select "*Bulk Read Cards*":

<img src="/assets/bulk-read-cards.png" class="screenshot sized-screenshot">

You'll be asked to fill in a card template for any new cards read during the bulk card read. Metadata will be taken from this template when new card data is read (the name will have an ascending per-run ID number added.)

<img src="/assets/set-template.png" class="screenshot sized-screenshot">

Select the start button to start the bulk read. You'll be returned to the wallet and a notification will appear to indicate that the bulk read is in progress in the background:

<img src="/assets/start-icon.png" class="screenshot" width="40">

<p class="arrow">↓</p>

<img src="/assets/bulk-read-notification.png" class="screenshot sized-screenshot">

Wave some physical cards over your device's antenna. Note that there is a deduplication filter - Walrus will ignore the same card being read more than once in succession. Either alternate between two different cards for testing purposes, or try a bunch of different ones.

Each card will lead to a new card appearing instantly in your wallet:

<img src="/assets/bulk-read-wallet.png" class="screenshot sized-screenshot">

Selecting the bulk read notification or the ongoing bulk reads toolbar icon in the wallet will show a listing of all ongoing bulk card reads:

<img src="/assets/bulk-reads-icon.png" class="screenshot" width="40">

<p class="arrow">↓</p>

<img src="/assets/bulk-reads.png" class="screenshot sized-screenshot">

By selecting a bulk card read, you can view its status or stop it:

<img src="/assets/bulk-read-dialog.png" class="screenshot sized-screenshot">

## Gotchas

As always in alpha software, there's a few known bugs that might bite you.

- Walrus's handling of device disconnections mid-operations is less than stellar in some cases.
- You might need to experiment with connecting your device directly vs. using a hub, and so on.

If you have any issues, we want to hear about it! Send us an [email](mailto:team@project-walrus.io) or file an issue on [GitHub](https://github.com/megabug/Walrus). We're still in alpha so bugs are to be expected, unfortunately 😅. With your help, however, we're committed to squashing all the bugs we can find!
