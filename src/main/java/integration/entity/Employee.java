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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="employee")
@NamedQuery(name="Employee.findAll", query="SELECT e FROM Employee e")
public class Employee implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int idemployee;

	//bi-directional many-to-many association to Asset
	@ManyToMany
	@JoinTable(
		name="employee_asset"
		, joinColumns={
			@JoinColumn(name="employee_idemployee")
			}
		, inverseJoinColumns={
			@JoinColumn(name="asset_idasset")
			}
		)
	private List<Asset> assets;

}