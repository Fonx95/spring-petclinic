package org.springframework.samples.farmatic.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
@Entity
@Table(name = "clientes")
public class Cliente extends Registrado {

	@NotBlank
	@Column(name = "provincia")
	private String				provincia;
	
	@Column(name = "localidad")
	@NotBlank
	private String				localidad;
	
	@Column(name = "direccion")
	private String				direccion;
	
	@Column(name = "porPagarTotal")
	private Double				porPagarTotal;
}
