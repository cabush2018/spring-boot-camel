package integration.camel;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter 
public class PersistNode {

	@JsonProperty
	String type;
	
	@JsonProperty
	Map<String, Object> properties = new HashMap<>();

	@JsonAnySetter
	public void prop(String k, Object v) {
		properties.put(k, v);
	}

	public Object prop(String k) {
		return properties.get(k);
	}

	public int getId() {
		return (int)prop("id");
	}

	public String getName() {
		return (String)prop("name");
	}
	
}
