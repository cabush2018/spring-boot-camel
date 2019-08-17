package integration.model;

import javax.annotation.PostConstruct;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.DynamicUpdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@DynamicUpdate
@Getter @Setter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Actor {
	
	@Id
	@Column(name = "source_id")
	private String sourceId;
	
	@Column(name="actor_id")
	private Integer id;
	
	private String name;
	
	@PostConstruct
	public void init() {
		if(id==null) {
			if(sourceId==null) {
				throw new RuntimeException("invalid");
			}
			id=sourceId.hashCode();
		}
	}
}