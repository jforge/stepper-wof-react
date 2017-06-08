package io.eol.tinkerforge.mqtt.router;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Configuration
@ComponentScan("io.eol.tinkerforge")
public class TinkerforgeMqttRouterApplication {
	static Logger log = Logger.getLogger(TinkerforgeMqttRouterApplication.class);

	@Autowired
	private EchoSubscriber echoSubscriber;

	private MqttAsyncClient client;

	private IMqttActionListener mqttConnectListener;

	private IMqttActionListener mqttDisconnectListener;


	public static void main(String[] args) {

		// http://www.slf4j.org/api/org/slf4j/impl/SimpleLogger.html
		System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
		// System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");

		SpringApplication.run(TinkerforgeMqttRouterApplication.class, args);
	}

	@PreDestroy
	public void shutdown() {
		log.debug("Shutting down Tinkerforge MQTT Router.");

		// TODO echoSubscriber.shutdown();
		// TODO graceful shutdown for connected devices and mqtt connections
	}
}
