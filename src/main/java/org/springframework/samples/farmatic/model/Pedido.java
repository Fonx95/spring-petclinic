package org.springframework.samples.farmatic.model;

import java.time.LocalDate;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.farmatic.model.Pedido.EstadoPedido;

import lombok.Data;

@Data
@Entity

public class Pedido extends BaseEntity{
	

	@Column(name = "codigo")
	@NotEmpty
	private String codigo;
	
	@Column(name = "fecha_pedido")
	@NotNull
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private LocalDate fechaPedido;
	
	@Column(name = "fecha_entrega")
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private LocalDate fechaEntrega;
	
	@Column(name = "Estado")
	@NotNull
	private EstadoPedido estadoPedido;
	
	@OneToMany(cascade = CascadeType.ALL)
	private Collection<LineaPedido> lineaPedido;
	
	public enum EstadoPedido {
		Pedido, Enviado, Recibido, Borrador;
	}

}
