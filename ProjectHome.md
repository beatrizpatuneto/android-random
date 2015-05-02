**UPDATE 2009-11-23**: Most of the software on this site has not been upgraded for the latest production releases of the Android platform. Some of the projects have moved on and others have actually seen their lifecycle to the end and have been implemented by Google. If you have any questions about any of the projects on the page feel free to ask. You can find a version of Glance built for 1.0 in the Android Market. Updates to Glance will be coming in the next few weeks and be posted to the Market as well as a Glance project page.

Thanks.

**UPDATE 2008-12-17**: Logcat has been ported to the G1 devices. Its a simple little apk that pipes the logcat output to the screen for easy viewing. Download the Logcat.apk on the right.


---


**UPDATE 2008-08-18**: Most of the code here is currently for M5.  We are working to update certain components to 0.9r1.

Reserved hosting space for a small collection of Android developer examples and general tinkering projects.

## Stuff we built to fiddle with Android on the HTC Vogue: ##

**[vogue-skin](http://android-random.googlecode.com/files/vogue-skin.tar)**: Simple 240x320px Android emulator skin which mimics the Vogue's display.

**[Messages](Messages.md)**: This is a fully integrated threaded text messaging application geared towards the Vogue.

**[Scribble](Scribble.md)**: A simple text editor to create/modify/view text based files on android. Scribble is also integrated with Glance with a context menu option to open files.

**[Glance](Glance.md)**: File manager to facilitate exploring and managing the system and data images from a running device.

**HttpDownloader** _(0.9r1)_: Android app to download files from the web directly to a running device.  We use this on the HTC Vogue to avoid manipulating the data.img file on the storage card each time we want to test a new APK.

**RemoteLogcat**: Android app to broadcast the logcat output to a remote server, so that we can watch real-time as we explore applications on the device.

**LogcatActivity**: Predecesor to RemoteLogcat, allowing you to view the Logcat output on the handset, and optionally save a limited history to a file on the device.

See http://it029000.massey.ac.nz/vogue/ for more information about running Android on an HTC Vogue (we're using the HTC Touch on Sprint).

## Misc. Android developer resources ##

**axml2xml.pl**: Tool to translate Android binary XML files to plain text XML.  Useful to run over system APKs, and the framework-res package to discover layout voodoo used by the Android team.

**aidl-cblistsub.pl** _(0.9r1)_: Utility to generate extended RemoteCallbackList classes to ease the burden of implementing callback listeners in a service.

**TestKeepAlive** _(1.0)_: Sophisticated demonstration of a persistent mobile TCP connection.  The logic here should closely match how Google implements "push" notification of e-mail, calendar, and Gtalk IM.

**Solitaire** _(0.9r1)_: Simple game implementation demonstrating various Android features, including elaborate custom drawing and state recovery.  Currently plays only Klondike however Freecell and Hearts are coming soon.

**JNITest**: Working example of JNI on Android, thanks to [Davanum Srinivas](http://davanum.wordpress.com/2007/12/09/android-invoke-jni-based-methods-bridging-cc-and-java/).  This also serves as a crude benchmark demonstrating the performance overhead of native reads versus a supplied InputStream.

**AsyncService**: Simple example of how to implement an asynchronous service on Android.  This is old code, maybe even for M3, but the concept still applies.

**AlphabetListView**: Widget I wrote early on in the development of [Five](http://five.googlecode.com) to support alphabet-based navigation.  I don't intend to use this moving forward, but it does serve as a good, robust example of custom widget creation.