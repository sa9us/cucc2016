package ind.sagus.dogsensor.mqtt;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Properties;

import org.springframework.beans.factory.DisposableBean;

import io.moquette.BrokerConstants;
import io.moquette.interception.InterceptHandler;
import io.moquette.proto.messages.PublishMessage;
import io.moquette.proto.messages.AbstractMessage.QOSType;
import io.moquette.server.Server;
import io.moquette.server.config.MemoryConfig;

public class MqttWebsocketBroker implements DisposableBean {
	
	private Server mqttServer = null;
	
	public MqttWebsocketBroker(String ws_port) throws IOException { 
		mqttServer = new Server();
		MemoryConfig cfg = new MemoryConfig(new Properties());
		cfg.setProperty(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, ws_port);	
		ArrayList<InterceptHandler> handlers = new ArrayList<InterceptHandler>();
		
		mqttServer.startServer(cfg, handlers);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (mqttServer != null)
					mqttServer.stopServer();
			}
		});
	}
	
	public void publishMsg(String topic, String msg) {
		PublishMessage pmsg = new PublishMessage();
		pmsg.setTopicName(topic);
		pmsg.setPayload(ByteBuffer.wrap(msg.getBytes()));
		pmsg.setQos(QOSType.MOST_ONE);
		
		try {
			mqttServer.internalPublish(pmsg);
		} catch (NullPointerException e) {
			System.err.println("server is stopped or failed to be initialized.");
		}
	}
	
	public void destroy() {
		if (mqttServer != null) {
			mqttServer.stopServer();
			mqttServer = null;
		}
	}
}
