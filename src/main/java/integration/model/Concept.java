package integration.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;

import integration.persistence.PersistNode;
import lombok.Data;

@Entity
@Data
public class Concept extends PersistNode {
	@Id
	@GeneratedValue
	@JsonProperty
	private int id;
	@JsonProperty
	private String name;
}
