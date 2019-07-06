package integration;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

public class PersistNode {

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	@JsonProperty
	private String type;
	
	@JsonProperty
	private Map<String, Object> properties = new HashMap<>();

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
