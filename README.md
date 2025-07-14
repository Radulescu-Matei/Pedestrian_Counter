DATA_APP - app for viewing data|
sensor.py - code for the esp32|
Sensor_Simulator_Link.txt - link to the wokwi emulator|
.pdf - explanation of the project


This project implements a miniature IoT system designed to simulate smart pedestrian traffic control at intersections. It uses motion sensors (simulating pressure sensors) embedded in the sidewalk to detect when pedestrians are waiting to cross. These detections are processed by an ESP32 microcontroller, which sends real-time data to an online MQTT broker (HiveMQ).

An Android application then retrieves the sensor data from the broker and displays live charts for each crosswalk, allowing users to monitor pedestrian flow. The app also allows users to set a configurable threshold for the number of pedestrians waiting at a red light. When this threshold is reached or exceeded, the system changes the traffic light to green for that crosswalk—replacing the traditional fixed-time approach.

The simulation is built using Wokwi for circuit emulation and relies on Python (MicroPython) code running on the ESP32. RGB LEDs are used to emulate traffic lights. Communication security is ensured via TLS over MQTT. The project demonstrates how low-cost IoT components can be combined with real-time data analysis and mobile visualization to improve urban traffic systems in a scalable and flexible way.
