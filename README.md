PebbLIFX
=========

PebbLIFX is a Pebble application that allows you to control your LIFX bulbs when your phone is in range of your bulb WiFi network. 

PebbLIFX uses the [LIFX Android SDK](https://github.com/LIFX/lifx-sdk-android) to communicate with the bulbs via a companion Android app.

Current Functionality
=============
* Get current list of bulbs on WiFi network.
* Display current state of bulbs (on/off).
* Toggle all/individual bulbs on/off.

Roadmap
=================
* Individual Bulb Menus
    - With addition of bulb menus, switch on/off to quick longpress action.
    - Individual bulb menus with current state.
    - Submenus: brightness with up/down button control, and color presets.
    - Back button will return to main menu.
* Android App UI
    - Pebble status (Connected)
    - Bulb List in-app
    - Toggle for PebbLIFX background service

Future Features
===============
* Custom color control in Android app
* Better error checking
    - Identification in watch app that phone is not connected to WiFi.
    - Faster response to watch actions (dependent on API).

