package ind.sagus.dogsensor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ind.sagus.dogsensor.mqtt.MqttWebsocketBroker;

public class AppConfig implements DisposableBean, ServletContextListener {
	
	private static final Log LOG = LogFactory.getLog(AppConfig.class);
	private static AppConfig instance = null;
	
	private Runnable clbkShutdown = null;
	
	private AbstractApplicationContext context;
	private ScheduledExecutorService simulator;
	private MqttWebsocketBroker mqttBroker;
	
	private Map<String, String> mqttConfig;
	private Map<String, Double> constants;
	private List<String> dogNames;
	
	public static AppConfig getInstance() { 
		if (instance == null) {
			AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {"config.xml"});
			ScheduledExecutorService ses = (ScheduledExecutorService) ctx.getBean("simulator");
			MqttWebsocketBroker mwb = (MqttWebsocketBroker) ctx.getBean("mqttBroker");
			instance = (AppConfig) ctx.getBean("constants");
			instance.setContext(ctx);
			instance.setSimulator(ses);
			instance.setMqttBroker(mwb);
		}
		
		return instance; 
	}

	public Map<String, String> getMqttConfig() {
		return mqttConfig;
	}

	public void setMqttConfig(Map<String, String> mqttConfig) {
		this.mqttConfig = mqttConfig;
	}

	public Map<String, Double> getConstants() {
		return constants;
	}

	public void setConstants(Map<String, Double> constants) {
		this.constants = constants;
	}

	public String[] getDogNames() {
		return dogNames.toArray(new String[0]);
	}

	public void setDogNames(List<String> dogNames) {
		this.dogNames = dogNames;
	}
	
	public void setShutdownCallback(Runnable clbkShutdown) {
		this.clbkShutdown = clbkShutdown;
	}

	public ScheduledExecutorService getSimulator() {
		return simulator;
	}

	public void setSimulator(ScheduledExecutorService simulator) {
		this.simulator = simulator;
	}

	public AbstractApplicationContext getContext() {
		return context;
	}

	public void setContext(AbstractApplicationContext context) {
		this.context = context;
	} 

	public MqttWebsocketBroker getMqttBroker() {
		return mqttBroker;
	}

	public void setMqttBroker(MqttWebsocketBroker mqttBroker) {
		this.mqttBroker = mqttBroker;
	}

	@Override
	public void destroy() throws Exception {
		if (clbkShutdown != null)
			clbkShutdown.run();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		context.close();
		
		LOG.info("waiting for 5000ms to clean up MqttWebsocketBroker...");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) { }
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
	}

}