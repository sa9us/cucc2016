package ind.sagus.dogsensor;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

import ind.sagus.dogsensor.model.DogInfo;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(DogSensorMonitorApplication.class);
	}
	
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		super.onStartup(servletContext);
		
		AppConfig cfg = AppConfig.getInstance();
		
		final int initDogs = cfg.getConstants().get("n_init_dogs").intValue();
		final String[] dognames = cfg.getDogNames();
		for (int i = 0; i < initDogs; i++) 
			new DogInfo(dognames[i]);	
		
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) { }
					DogInfo.simulate();
				}
			}
		}).start();
	}
}
