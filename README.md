# Android Wear Demo

## Overview

This app provides a simple showcase for the basic tools available when developing Android Wear apps, with a focus on custom UI and data syncing. The main Wear app consists of a list with the following options:

- **Bad Layout Demo** : An example of an activity whose inflated layout will not fit correctly on a round device
- **BoxInset Demo** : Uses `BoxInsetLayout` to take the original layout from the first activity and correct it for round devices
- **WatchViewStub Demo** : Uses `WatchViewStub` to inflate separate layouts for square and round devices
- **CardFragment Demo** : A basic example of creating a `CardFragment`
- **CardFrame Demo** : A basic example of including a `CardFrame` in a layout in order to provide arbitrary card content
- **2D Picker Demo** : Demonstrates the "2D Picker" pattern by making use of a `GridViewPager`
- **DelayedConfirmationView Demo** : Simple example of including a `DelayedConfirmationView` in a layout
- **DismissOverlayView Demo** : Demonstrates the usage of the `DismissOverlayView` to implement the long-press-to-dismiss pattern

In addition, the primary Activity of the Wear app should be understood as a demonstration of the `WearableListView`.

## Setup

To grab the repo:

    $ git clone git@github.com:livefront/android-wear-demo.git

All the demos can be tested by loading the `wear` module directly onto a device or emulator in the standard way using Android Studio.