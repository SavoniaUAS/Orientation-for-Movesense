# Orientation-for-Movesense
Android application that uses the Suunto Movesense sensor to track an object's orientation

A Complementary filter algorithm is used to estimate a Movesense sensor orientation. Alternative algorithms Madgwick and Mahony are included in the source code. They may not work as expected because of non-existent magnetometer calibration algorithm.

Android application details:
<ul>
  <li>Minimum SDK version 23 (Android 6.0)</li>
  <li>Target SDK version 29 (Android 10.0)</li>
</ul>

Used application permissions:
<ul>
  <li>“android.permission.BLUETOOTH”</li>
  <li>“android.permission.BLUETOOTH_ADMIN”</li>
  <li>“android.permission.ACCESS_FINE_LOCATION”
		<ul><li>A runtime permission request is required.</li></ul>
</ul>
  
Known bugs:
<ul>
	<li>Magnetometer calibration needs a proper algorithm.</li>
	<li>Changing a configuration of gyroscope or magnetometer sensor produces failure of Whiteboard HTTP 400 or 503.</li>
</ul>
 

Documentation: https://savoniauas.github.io/Orientation-for-Movesense/. Code description and comments are in Finnish.




