package ind.sagus.dogsensor.model;

import ind.sagus.dogsensor.AppConfig;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DogInfo {
	private static final HashMap<Integer, DogInfo> dogs = new HashMap<Integer, DogInfo>();
	private static int nextId = 1;
	
	private double dirx, diry;
	private float norm_hb, norm_tempr;
	
    public int id;
	public String name;
	public float weight;
	public float heartBeat;
	public float temperature;
	
	@JsonProperty("long")
	public double lng;
	public double lat;
	
	DogInfo() { }
	
	public DogInfo(String name) {		
		this(name, 0, 0, 0);
		
		Random r = new Random();
		Map<String, Double> c = AppConfig.getInstance().getConstants();
		
		weight = (float) (c.get("s_weight") + c.get("r_weight") * r.nextDouble());
		heartBeat = (float) (c.get("s_heartbeat") + c.get("r_heartbeat") * r.nextDouble());
		temperature = (float) (c.get("s_tempr") + c.get("r_tempr") * r.nextDouble());
		
		weight -= weight % .1f;
		heartBeat -= heartBeat % .1f;
		temperature -= temperature % .01f;
		
		norm_hb = heartBeat;
		norm_tempr = temperature;
	}
	
	public DogInfo(String name, float weight, float heartbeat, float tempr) {		
		this(name, weight, heartbeat, tempr, 0, 0);
		
		Random r = new Random();
		Map<String, Double> c = AppConfig.getInstance().getConstants();
		this.lng = c.get("s_lng") + c.get("r_lng") * r.nextDouble();
		this.lat  = c.get("s_lat") + c.get("r_lat") * r.nextDouble();
	}
	
	public DogInfo(String name, float weight, float heartbeat, float tempr, double lng, double lat) {
		Map<String, Double> c = AppConfig.getInstance().getConstants();
		double min_x  = c.get("s_lng");
		double max_x  = c.get("s_lng") + c.get("r_lng");
		double min_y  = c.get("s_lat");
		double max_y  = c.get("s_lat") + c.get("r_lat");
		
		this.id = nextId++;
		this.name = name;
		this.weight = weight;
		this.heartBeat = heartbeat;
		this.temperature = tempr;
		this.lng = Math.min(max_x, Math.max(min_x, lng));
		this.lat = Math.min(max_y, Math.max(min_y, lat));
		
		double movedir = new Random().nextDouble() * Math.PI * 2;
		this.dirx = Math.sin(movedir);
		this.diry = Math.cos(movedir);

		norm_hb = heartbeat;
		norm_tempr = tempr;
		
		dogs.put(this.id, this);
	}
	
	public static DogInfo[] getAllDogs() {
		return dogs.values().toArray(new DogInfo[0]);
	}
	
	public static DogInfo getDogById(int id) {
		return dogs.get(id);
	}
	
	public static void simulate() {
		Random r = new Random();
		
		for (DogInfo i : dogs.values()) {
			Map<String, Double> c = AppConfig.getInstance().getConstants();
			double min_x  = c.get("s_lng");
			double max_x  = c.get("s_lng") + c.get("r_lng");
			double min_y  = c.get("s_lat");
			double max_y  = c.get("s_lat") + c.get("r_lat");
			double coef_x = c.get("o_lng");
			double coef_y = c.get("o_lat");
			double speed  = r.nextGaussian() / 4 + .5; // 95% of values will fall between 0 to 1
			double mov_x  = coef_x * speed * i.dirx;
			double mov_y  = coef_y * speed * i.diry;
			
			i.lng += mov_x;
			i.lat  += mov_y;
			if (i.lng > max_x) {
				i.lng = 2 * max_x - i.lng;
				i.dirx *= -1;
			}
			if (i.lng < min_x) {
				i.lng = 2 * min_x - i.lng;
				i.dirx *= -1;
			}
			if (i.lat > max_y) {
				i.lat = 2 * max_y - i.lat;
				i.diry *= -1;
			}
			if (i.lat < min_y) {
				i.lat = 2 * min_y - i.lat;
				i.diry *= -1;
			}
			
			double incRatio = c.get("o_inc_ratio");
			i.heartBeat = (float) (i.norm_hb + c.get("r_heartbeat") * speed * incRatio);
			i.temperature = (float) (i.norm_tempr + c.get("r_tempr") * speed * incRatio);
			i.heartBeat -= i.heartBeat % .1f;
			i.temperature -= i.temperature % .01f;
		}	
	}

	@SuppressWarnings("unused")
	public static Object getClusters(int k, boolean showWeight) {
		Dataset[] res = __KMeansFunc(k);
		
		Object[][] _value = new Object[res.length][];
		for (int i = 0; i < res.length; i++) {
			_value[i] = res[i].stream()
				.map(x -> showWeight ? new Object() { 
						public int id = (int)x.classValue(); 
						public float w = (float) x.value(0);
					} : new Object() { 
						public int id = (int)x.classValue(); 
					}
				)
				.toArray();
		}
		return new Object() {
			public int numberOfClusters = res.length;
			public Object[][] value = _value;
		};
	}
	
	public static String getSimplyRenderedClusters(int k) {
		String[] colors = new String[]{
			"red", "blue", "orange", "cyan", "purple"
		};
		Map<String, Double> c = AppConfig.getInstance().getConstants();
		double min_x  = c.get("s_lng");
		double min_y  = c.get("s_lat");
		
		String ret = "";
		int ci = 0;
		for (Dataset i : __KMeansFunc(k)) {
			for (Instance j : i) {
				ret += String.format(
					"<p style='position:fixed;top:%dpx;left:%dpx;color:%s'>%s-%.1f</p>", 
					(int)((getDogById((int) j.classValue()).lng - min_x) * 5e4) + 10, 
					(int)((getDogById((int) j.classValue()).lat  - min_y) * 2e5) + 10,
					colors[ci % 5],
					j.classValue().toString(),
					j.value(0)
				);
			}
			ci++;
			ret += "\n";
		}
		
		return ret;
	}
	
	private static Dataset[] __KMeansFunc(int k) {
		KMeans km = new KMeans(k, 100);
		Dataset d = new DefaultDataset();
		for (DogInfo i : dogs.values()) {
			Instance ins = new DenseInstance(new double[] {
				i.weight
			}, i.id);
			d.add(ins);
		}
		Dataset[] res = km.cluster(d);
		
		return res;
	}
}