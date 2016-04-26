package ind.sagus.dogsensor;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AppConfig {
	
	private static AppConfig instance = null;
	
	private Map<String, Double> constants;
	private List<String> dogNames;
	
	public static AppConfig getInstance() { 
		if (instance == null) {
			@SuppressWarnings("resource")
			ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {"config.xml"});
			instance = (AppConfig) ctx.getBean("constants");
		}
		
		return instance; 
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

}