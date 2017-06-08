import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
var Tinkerforge = require('tinkerforge');

  var deg = 0;
  var maxVelocity = 15000;
  var maxAccel = 50000;
  var maxDecel = 4000;

    function getRandomInt(min, max) {
        min = Math.ceil(min);
        max = Math.floor(max);
        return Math.floor(Math.random() * (max - min +1)) + min;
    }

    function onClick() {
        this.removeAttribute('style');
        var rnd = getRandomInt(0, 500);
        deg = deg + (500 + rnd);
        var css = '-webkit-transform: rotate(' + deg + 'deg);';
        this.setAttribute(
            'style', css
        );

		var ipcon;
        var HOST = 'localhost';
        var PORT = 4280;
        var UID = '62Ydqh';
        var stepper;

        ipcon = new Tinkerforge.IPConnection(); // Create IP connection
        stepper = new Tinkerforge.BrickStepper(UID, ipcon); // Create device object

		ipcon.connect(HOST, PORT,
			function(error) {
				console.log('Error: ' + error + '\n');
			}
		); // Connect to brickd
	    // Don't use device before ipcon is connected

		ipcon.on(Tinkerforge.IPConnection.CALLBACK_CONNECTED,
			function (connectReason) {
				stepper.setMotorCurrent(800); // 800mA
				stepper.setStepMode(8); // 1/8 step mode
				stepper.setMaxVelocity(maxVelocity); // Velocity 2000 steps/s

				// Slow acceleration (500 steps/s^2),
				// Fast deacceleration (5000 steps/s^2)
				stepper.setSpeedRamping(maxAccel, maxDecel + rnd);
				stepper.setCurrentPosition(0);
				stepper.enable(); // Enable motor power

				var steps = 40000 - rnd * 10
				stepper.setSteps(steps); // Drive 60000 steps forward
			}
		);

		// Register position reached callback
		stepper.on(Tinkerforge.BrickStepper.CALLBACK_POSITION_REACHED,
			// Use position reached callback to program random movement
			function (position) {
		        console.log("Position reached. Disconnect device. " + position);
		}
    	);

    	//	shutdown(stepper,ipcon);

	}

    function shutdown(stepper,ipcon) {
		try {
           if(stepper !== undefined) {
                stepper.stop();
                stepper.disable();
            }

            if(ipcon !== undefined) {
                ipcon.disconnect();
            }
        } catch (Exception) {
            console.log("Device access problem.");
        }
    }


class App extends Component {
  componentDidMount() {
    var img = document.querySelector('.spinner');
    img.addEventListener('click', onClick, false);
  }
  render() {
    return (
      <div className="App">
        <div className="App-header">
          <img src={logo} className="App-logo" alt="logo" />
          <h2>Welcome to React</h2>
        </div>
        <p className="App-intro">
          <img className="spinner" width="400" height="400" src="http://www.clker.com/cliparts/4/M/o/y/T/w/wheel-of-fortune-hi.png" />
        </p>
      </div>
    );
  }
}

export default App;
