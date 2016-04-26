package ind.sagus.dogsensor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ind.sagus.dogsensor.model.DogInfo;

@CrossOrigin
@RestController
@RequestMapping("/dogs")
@SpringBootApplication
public class DogSensorMonitorApplication {

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	DogInfo getDog(@PathVariable int id) {
		DogInfo res = DogInfo.getDogById(id);
		if (res == null)
			throw new ParameterException("Id", Integer.toString(id));
		
		return res;
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	DogInfo addDog(@RequestBody DogInfo input) {
		return new DogInfo(
			input.name, input.weight, input.heartBeat, input.temperature,
			input.lng, input.lat
		);	
	}
	
	@RequestMapping(method = RequestMethod.GET)
	DogInfo[] listAllDogs() {
		return DogInfo.getAllDogs();
	}
	
	@RequestMapping(method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.OK)
	void __delete() { }

	public static void main(String[] args) {		
		SpringApplication.run(DogSensorMonitorApplication.class, args);
	}

}

@CrossOrigin
@RestController
@RequestMapping("/dogClusters")
class DogClusteringController {
	
	@RequestMapping(method = RequestMethod.GET)
	Object dogClustering(
			@RequestParam("k") int k,
			@RequestParam(value = "showWeight", required = false) String sw
	) {
		if (k < 1)
			throw new ParameterException("k", Integer.toString(k));

		return DogInfo.getClusters(k, sw != null);
	}
	
	@RequestMapping(value="/rendered", method = RequestMethod.GET)
	String dogClusteringSimplyRendered(@RequestParam("k") int k) {
		if (k < 1)
			throw new ParameterException("k", Integer.toString(k));

		return DogInfo.getSimplyRenderedClusters(k);
	}
	
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class ParameterException extends RuntimeException {
	private static final long serialVersionUID = -5287669427340903411L;

	public ParameterException(String k, String v) {
		super(String.format("Illegal value %s for %s", v, k));
	}
}