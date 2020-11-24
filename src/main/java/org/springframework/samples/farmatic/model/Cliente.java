package org.springframework.samples.farmatic.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
@Entity

public class Cliente extends Registrado {

	@NotBlank
	private String				provincia;
	@NotBlank
	private String				localidad;
	
	private String				direccion;
	
	private Double				porPagarTotal;
}