package integration.model;


import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

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
@Embeddable
public class Audit implements Serializable{

	protected String operation;

	@Column(name = "created_on")
	protected Date createdOn;

	@Column(name = "created_by")
	protected String createdBy;

}