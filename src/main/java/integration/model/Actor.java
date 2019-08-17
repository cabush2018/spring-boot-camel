package integration.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.PersistenceException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({IdListener.class})
public class Actor extends AbstractAsset {

	@Id
	@Column(name = "source_id")
	protected String sourceId;

	@Column(name = "actor_id")
	protected Integer id;

	private String name;

}