import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
var Tinkerforge = require('tinkerforge');

  var deg = 0;

    function onClick() {
        this.removeAttribute('style');
        deg = deg + (500 + Math.round(Math.random() * 500));
        var css = '-webkit-transform: rotate(' + deg + 'deg);';
        this.setAttribute(
            'style', css
        );

        var ipcon;
        var HOST = 'localhost';
        var PORT = 4280;
        var UID = '62Ydqh';
        if(ipcon !== undefined) {
            ipcon.disconnect();
        }
        ipcon = new Tinkerforge.IPConnection(); // Create IP connection
        var stepper = new Tinkerforge.BrickStepper(UID, ipcon); // Create device object

        stepper.setResponseExpected(Tinkerforge.BrickletRemoteSwitch.FUNCTION_SWITCH_SOCKET_B, true);

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
                stepper.setMaxVelocity(2000); // Velocity 2000 steps/s

                // Slow acceleration (500 steps/s^2),
                // Fast deacceleration (5000 steps/s^2)
                stepper.setSpeedRamping(500, 5000);

                stepper.enable(); // Enable motor power
                stepper.setSteps(60000); // Drive 60000 steps forward
           }
        );
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
          <img className="spinner" width="400" height="400" src="http://www.clker.com/cliparts/4/M/o/y/T/w/wheel-of-fortune-hi.png" />.
        </p>
      </div>
    );
  }
}

export default App;
