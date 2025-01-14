import time
import network
from umqtt.simple import MQTTClient
import random
from machine import Pin, PWM

SSID = "Wokwi-GUEST"
PASSWORD = ""

BROKER = "broker.hivemq.com"
PORT = 8883
TOPIC = "crosswalk/esp32"
CLIENT_ID = "esp32_crosswalk"

# LED Pin Configuration
RED_PIN_1, GREEN_PIN_1, BLUE_PIN_1 = 25, 26, 27
RED_PIN_2, GREEN_PIN_2, BLUE_PIN_2 = 14, 13, 12

# Initialize LEDs
red_light_1 = PWM(Pin(RED_PIN_1), freq=1000, duty=0)
green_light_1 = PWM(Pin(GREEN_PIN_1), freq=1000, duty=0)
blue_light_1 = PWM(Pin(BLUE_PIN_1), freq=1000, duty=0)

red_light_2 = PWM(Pin(RED_PIN_2), freq=1000, duty=0)
green_light_2 = PWM(Pin(GREEN_PIN_2), freq=1000, duty=0)
blue_light_2 = PWM(Pin(BLUE_PIN_2), freq=1000, duty=0)

# Wi-Fi Connection
wlan = network.WLAN(network.STA_IF)
wlan.active(True)
wlan.connect(SSID, PASSWORD)
while not wlan.isconnected():
    print("Connecting to Wi-Fi...")
    time.sleep(1)
print("Connected to Wi-Fi!")

# MQTT Connection
def connect_mqtt():
    try:
        client = MQTTClient(CLIENT_ID, BROKER, port=PORT, ssl=True)
        client.connect()
        print(f"Connected to broker: {BROKER}")
        return client
    except Exception as e:
        print(f"Failed to connect to MQTT broker: {e}")
        return None

# Initialize
pedestrians_waiting_1, pedestrians_waiting_2 = 0, 0
last_light_switch_time = time.time()
is_crosswalk_1_green = False

# Set initial light states
red_light_1.duty(0)
green_light_1.duty(1023)
red_light_2.duty(1023)
green_light_2.duty(0)

# Main loop
try:
    client = connect_mqtt()
    if not client:
        print("Exiting due to MQTT connection failure.")
    else:
        while True:
            # Update pedestrian counts based on light states
            if is_crosswalk_1_green:
                pedestrians_waiting_1 = 0  # Crosswalk 1 is green, no waiting pedestrians
                pedestrians_waiting_2 += random.randint(1, 5)  # Crosswalk 2 is red
            else:
                pedestrians_waiting_2 = 0  # Crosswalk 2 is green, no waiting pedestrians
                pedestrians_waiting_1 += random.randint(1, 5)  # Crosswalk 1 is red

            # Publish updated data
            client.publish(TOPIC, f"Crosswalk 1 - Pedestrians waiting: {pedestrians_waiting_1}")
            client.publish(TOPIC, f"Crosswalk 2 - Pedestrians waiting: {pedestrians_waiting_2}")

            print(f"Crosswalk 1 - Pedestrians waiting: {pedestrians_waiting_1}")
            print(f"Crosswalk 2 - Pedestrians waiting: {pedestrians_waiting_2}")

            # Toggle lights every 20 seconds
            if time.time() - last_light_switch_time >= 20:
                if is_crosswalk_1_green:
                    # Crosswalk 1 green -> Crosswalk 2 green
                    red_light_1.duty(1023)
                    green_light_1.duty(0)
                    red_light_2.duty(0)
                    green_light_2.duty(1023)
                else:
                    # Crosswalk 2 green -> Crosswalk 1 green
                    red_light_1.duty(0)
                    green_light_1.duty(1023)
                    red_light_2.duty(1023)
                    green_light_2.duty(0)

                # Toggle the state
                is_crosswalk_1_green = not is_crosswalk_1_green
                last_light_switch_time = time.time()

            time.sleep(5)
except Exception as e:
    print(f"Error occurred: {e}")
