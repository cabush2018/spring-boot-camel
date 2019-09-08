package integration.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="element")
@NamedQuery(name="Element.findAll", query="SELECT e FROM Element e")
public class Element implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int idelement;

	private String name;

	//bi-directional many-to-many association to Asset
	@ManyToMany(mappedBy="elements")
	private List<Asset> assets;

	//bi-directional many-to-one association to Part
	@OneToMany(mappedBy="element")
	private List<Part> parts;

	public Part addPart(Part part) {
		getParts().add(part);
		part.setElement(this);

		return part;
	}

	public Part removePart(Part part) {
		getParts().remove(part);
		part.setElement(null);

		return part;
	}

}