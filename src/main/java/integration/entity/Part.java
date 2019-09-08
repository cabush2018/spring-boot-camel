package integration.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="part")
@NamedQuery(name="Part.findAll", query="SELECT p FROM Part p")
public class Part implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int partid;

	//bi-directional many-to-one association to Element
	@ManyToOne
	private Element element;

}