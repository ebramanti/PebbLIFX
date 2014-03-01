Initial Goals
=============
* Activation of companion app when Pebble app opens.
    - Start Node server when app is opened on watch.
    - Close server when app is closed on watch.
* Light commands in Pebble app
    - Brightness
    - On/Off
* Option for All Bulbs and Individual Bulbs
    - Use Node server to detect currently connected bulbs.
    - Use optional parameters to control individual bulbs.

Pebble App Functionality
=================
* Initial Opening
    - Shows loading (maybe a splash) screen while companion app loads Node server and gets details.
* Main Menu
    - Shows the current lists of bulbs on the WiFi network.
        + Scrollable using up/down keys.
    - First result always shows "All Bulbs" with subsequent bulbs listed below
    - Bulbs pull bulb names from companion app running node server
        + If no name given for bulb, use "New Bulb" as a default in Pebble app code.
* Option Selected
    - Up and down keys will control brightness.
    - Middle button will control on/off status.
    - Bulb name/functionality prompts will appear on screen.
    - Back button will return to main menu.

Future Features
===============
* Possible Color Preset Options for all Bulbs
    - LED White
    - Incandescent
    - Custom Color
* Addition to app of custom profiles pushed to watch app
    - Not sure how to do that without a custom build.
* Better error checking
    - Identification in watch app that phone is not connected to WiFi.
    - Integration of inevitable LIFX API into this application.
