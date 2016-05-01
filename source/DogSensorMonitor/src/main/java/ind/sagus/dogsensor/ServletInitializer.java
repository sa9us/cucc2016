package ind.sagus.dogsensor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import ind.sagus.dogsensor.model.DogInfo;
import ind.sagus.dogsensor.mqtt.MqttWebsocketBroker;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(DogSensorMonitorApplication.class);
	}
	
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		super.onStartup(servletContext);
		
		AppConfig cfg = AppConfig.getInstance();
		servletContext.addListener(cfg);
		
		final int initDogs = cfg.getConstants().get("n_init_dogs").intValue();
		final String[] dognames = cfg.getDogNames();
		for (int i = 0; i < initDogs; i++) 
			new DogInfo(dognames[i]);	
		
		final ObjectMapper mapper = new ObjectMapper();
		final MqttWebsocketBroker mqtt = cfg.getMqttBroker();
		final ScheduledExecutorService ses = cfg.getSimulator();
		final String topicName = cfg.getMqttConfig().get("topic_name");
		final int refreshrate = cfg.getConstants().get("n_refresh_rate").intValue();
		
		ses.scheduleAtFixedRate(new Runnable() {
			public void run() {
				DogInfo.simulate();
				try {
					String msg = mapper.writeValueAsString(DogInfo.getAllDogs());
					mqtt.publishMsg(topicName, msg);
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		}, refreshrate, refreshrate, TimeUnit.MILLISECONDS);
	}
}
