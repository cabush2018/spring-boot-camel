package integration.entity;

import java.io.Serializable;
import javax.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="asset")
@NamedQuery(name="Asset.findAll", query="SELECT a FROM Asset a")
public class Asset implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int idasset;

	private String name;

	//bi-directional many-to-many association to Element
	@ManyToMany
	@JoinTable(
		name="asset_element"
		, joinColumns={
			@JoinColumn(name="asset_idasset")
			}
		, inverseJoinColumns={
			@JoinColumn(name="element_idelement")
			}
		)
	private List<Element> elements;

	//bi-directional many-to-many association to Employee
	@ManyToMany(mappedBy="assets")
	private List<Employee> employees;
}