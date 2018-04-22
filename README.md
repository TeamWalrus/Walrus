# Walrus

*Make the most of your card cloning devices.*

Walrus enables you to use your existing contactless card cloning devices with your Android device. Using a simple interface, cards can be read into a wallet to be written or emulated later.

Designed for physical security assessors, Walrus has features that will help you in your next red team engagement.

**For end-user information such as what Walrus is, how to install it and how to use it, check out the [Walrus website](http://project-walrus.io/)! This readme outlines information intended for developers.**

## Licensing

Walrus is developed by Daniel Underhay and Matthew Daley (a.k.a. [Team Walrus](mailto:team@project-walrus.io)!) and is [open source](https://github.com/megabug/Walrus), released under the [GNU General Public License v3.0](https://github.com/megabug/Walrus/blob/master/LICENSE).

## Building

Walrus is a standard Android Studio project. At this stage there are no unusual dependencies or build steps beyond the usual cloning of the repository and opening the project in Android Studio.

TODO: When we refresh and remove the current Google Maps API key from the repo, we'll need to point out that this needs to be generated and set manually if maps are needed.

## Testing

We don't currently have a test suite, we're naughty ;\_;

## Codebase

The current layout of Walrus's source code is as follows:

* `/app/src/main`

  * `/assets`: Any non-resource assets, like the open source license listing.

  * `/res`: Resource files.

  * `/java/com/bugfuzz/android/projectwalrus`: Actual code lives here!

    * `/card`: Code to do with persistent data (i.e. the wallet). The `Card` class, the base `CardData` class and various card data type classes, database models and database helpers are here.

    * `/device`: Device-agnostic and device-specific driver code. The important `CardDeviceManager` lives here alongside the base `CardDevice` class and its child classes for various basic kinds of device (serial, line-based, etc.). Code to handle bulk reading is also located here.

      * `/proxmark3`: Proxmark3 driver code.

      * `/chameleonmini`: Surprise! Chameleon Mini driver code.

    * `/ui`: Code to do with other UI.

    * `/util`: Miscellaneous.

## Contributing

We welcome all kinds of contributions and bug reports, big or small! Development takes place at our [GitHub repository](https://github.com/megabug/Walrus). There you can file issues (both bugs and enhancement requests) and submit pull requests.

During the initial development of Walrus, changes to the codebase are likely to be frequent and wide-ranging, so if you want to work on a feature, it's wise to reach out first to ensure that your hard work won't be soon obsoleted. After our first full release we hope to gain stability and bring in some of the additional resources expected of a project today, such as a proper test suite and continuous integration.

One area we'd love your help with is contributing translations! If you think you can help us out translating our [Android string resources](https://github.com/megabug/Walrus/blob/master/app/src/main/res/values/strings.xml) to another language, please get in touch!
