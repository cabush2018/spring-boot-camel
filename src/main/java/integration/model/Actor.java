package integration.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners({ IdListener.class })
//@EntityListeners({ AuditListener.class, IdListener.class })
public class Actor extends AbstractAsset {

	@Id
	@Column(name = "source_id")
	protected String sourceId;

	@Column(name = "actor_id")
	protected Integer id;

	protected String name;

}