package integration.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
@Table(name="asset")
@NamedQuery(name="Asset.findAll", query="SELECT a FROM Asset a")
public class Asset extends AbstractAsset implements Serializable {
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