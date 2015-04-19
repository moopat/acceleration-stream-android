# acceleration-stream-android
Android client for the Acceleration Stream. The app will receive the acceleration data sent by the [corresponding Pebble WatchApp](https://github.com/moopat/acceleration-stream-pebble).

## Basic Usage
Check out this project in Android Studio and 
the [Pebble Project](https://github.com/moopat/acceleration-stream-pebble) 
in [CloudPebble](https://cloudpebble.net). Both IDEs offer GitHub support out of the box.

After checking out, make sure that the UUID 
in [MainActivity.java](https://github.com/moopat/acceleration-stream-android/blob/master/app/src/main/java/at/eht/stream/MainActivity.java) 
and the UUID in the settings of your CloudPebble project match. If you modify the data transmission please
be aware that some of the constants in [MainActivity.java](https://github.com/moopat/acceleration-stream-android/blob/master/app/src/main/java/at/eht/stream/MainActivity.java)
and [main.c](https://github.com/moopat/acceleration-stream-pebble/blob/master/src/main.c) have to match for the data transmission to work correctly.

The WatchApp will show the time and the result (success, error) of the last transmission, whereas the Android app 
will show the number of Samples received in the current session. By default the last 500 Samples 
(= 50 seconds of data) are retained and available for export to CSV. If you need longer data retention and the possibility to transmit the data to a server check out [acceleration-recorder-android](https://github.com/moopat/acceleration-recorder-android) which was originally forked from here.
