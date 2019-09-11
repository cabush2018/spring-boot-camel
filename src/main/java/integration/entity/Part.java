package integration.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import integration.model.AbstractAsset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="part")
@NamedQuery(name="Part.findAll", query="SELECT p FROM Part p")
public class Part extends AbstractAsset implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="partid")
	private Integer idpart;

	private String name;

	//bi-directional many-to-one association to Element
	@ManyToOne
	private Element element;

}