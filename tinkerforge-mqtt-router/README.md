# Tinkerforge Mqtt Router

Module to route and translate simplified incoming control messages 
e.g. from an AWS Lambda service to messages for Tinkerforge Hardware.

Such hardware can be controlled directly (e.g. stepper motor) or by
using a Tinkerforge Brick MQTT Proxy that expects a specific json 
payload format and a certain MQTT topic structure.

The hardware is currently not configured directly in the lambda service
using environment variables in order to be more flexible in choice of 
hardware (e.g. multiple segment displays) and for faster integration.

Yes, this could also be due to lack of trust in the cloud services,
so we have full control about hardware and processes until there is
real proof of maximum security e.g. for the stepper motor.

## Workflows

### Supported devices

#### Stepper motor

The stepper motor is a component without a brick mqtt proxy mapping.
so a native control via Tinkerforge Brick daemon is required.

Incoming simplified message structure, e.g. 
- Topic "echo/motor"
- Message "move;forward;1000"

reflects the content of a Alexa Skill utterance to be mapped to
a format the hardware components understand.
The payload is parsed and mapped directly to the Brick API.

### Segment display

The segment display is an actuator with a brick mqtt proxy mapping,
so an indirect re-routing to a tinkerforge mqtt topic with a
specific json payload format is all that needs to be done.

Example Tinkerforge Segment Count from 100 to 0:

Incoming simplified message structure, e.g. 
- Topic "echo/count"
- Message "100;0;-1;1000" (start,stop, increment, length in ms)

Outgoing brick proxy message:
- Topic

``tinkerforge/bricklet/segment_display_4x7/kTQ/start_counter/set
``
``tinkerforge/bricklet/segment_display_4x7/pVw/start_counter/set
``

- Messages:

``{ "value_from": 100, "value_to": 0, "increment": -1, "length": 1000}
``

``{ "value_from": 100, "value_to": 250, "increment": 3, "length": 1000}
``

#### LCD display

The 20x4 LCD display is an actuator with a brick mqtt proxy mapping,
so an indirect re-routing to a tinkerforge mqtt topic with a
specific json payload format is all that needs to be done.

Example Tinkerforge Display any test:

Incoming simplified message structure, e.g. 
- Topic "echo/display"
- Payload "text"

Outgoing brick proxy message:
- Topic

``tinkerforge/bricklet/lcd_20x4/o6J/clear_display/set
``
``tinkerforge/bricklet/lcd_20x4/o6J/backlight_on/set
``
``tinkerforge/bricklet/lcd_20x4/o6J/write_line/set
``

- Message:

``{ "line" : 0, "position" : 0, "text": "your_text" }
``

#### Dual Button

...

#### Other actuators

...

#### Sensors

...

