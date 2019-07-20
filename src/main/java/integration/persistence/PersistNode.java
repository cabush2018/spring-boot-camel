package integration.persistence;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.ToString;

@Getter 
@ToString 
@Builder
public class PersistNode {

	private String type;
	
	@Default
	private Map<String, Object> properties = new HashMap<>();

}
