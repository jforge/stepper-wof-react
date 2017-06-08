# stepper-wof-react
Stepper Motor driven Wheel of Fortune React.js App

## WoF Stepper App
React.js App for controlling the stepper motor (directly or decoupled using MQTT)



## Tinkerforge-mqtt-router
MQTT subscriber and publisher as content-based router and
message translator to create mqtt-brick-proxy compatible 
message structures, when consuming simplified device messages.


## Wheel of Fortune Stepper Motor flow

The existing secure infrastructure should be used by the Lambda service, further specific cloud services should be avoided.
For this infrastructure is up, running and secure, the Alexa skill is just a Voice UI add-on.

![alt text][wof-stepper-flow]

[wof-stepper-flow]: https://github.com/jforge/stepper-wof-react/raw/master/src/docs/images/wof-stepper-flow.png "WoF Stepper Flow"


## Purpose of this sample

- We did it with React.js to learn more about React and reactive programming.
- We wanted to see
  - fast and functional web application setup with minimal footprint
  - successful node/react integration with the Tinkerforge JavaScript API to control the stepper motor
  - successful node/react integration with the Eclipse Paho mqtt JavaScript library

- Results:
  - always decouple hardware from web apps using asynchronous approaches (KISS: use mqtt with paho)
  - ... tbd.

