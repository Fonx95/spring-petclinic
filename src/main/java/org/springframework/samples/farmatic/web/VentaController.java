
package org.springframework.samples.farmatic.web;

import java.util.Collection;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.farmatic.model.Cliente;
import org.springframework.samples.farmatic.model.Comprador;
import org.springframework.samples.farmatic.model.LineaVenta;
import org.springframework.samples.farmatic.model.Producto;
import org.springframework.samples.farmatic.model.TipoProducto;
import org.springframework.samples.farmatic.model.Venta;
import org.springframework.samples.farmatic.model.Venta.EstadoVenta;
import org.springframework.samples.farmatic.service.ClienteService;
import org.springframework.samples.farmatic.service.ProductoService;
import org.springframework.samples.farmatic.service.VentaService;
import org.springframework.samples.farmatic.service.exception.LineaVentaStockException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class VentaController {

	private final VentaService		ventaService;

	private final ProductoService	productoService;

	private final ClienteService	clienteService;


	@Autowired
	public VentaController(final VentaService ventaService, final ProductoService productoService, final ClienteService clienteService) {
		this.ventaService = ventaService;
		this.productoService = productoService;
		this.clienteService = clienteService;
	}

	@InitBinder
	public void setAllowedFields(final WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("ventaActual")
	public Venta getVentaActual() {
		Venta venta = this.ventaService.ventaActual();
		return venta;
	}

	@GetMapping(value = "/ventas")
	public String listVentas(final ModelMap model) {
		Collection<Venta> ventas = this.ventaService.findAllVentas();
		model.put("ventas", ventas);
		VentaController.log.info("Se han mostrado " + ventas.size() + " ventas");
		return "ventas/ventaList";
	}

	@GetMapping(value = "/ventas/{ventaId}")
	public String detallesVentas(@PathVariable("ventaId") final Venta venta, final ModelMap model) {
		if (venta.getEstadoVenta() == EstadoVenta.enProceso) {
			return "redirect:/ventas/actual";
		} else {
			model.put("venta", venta);
			VentaController.log.info("Se ha mostrado la venta con el id " + venta.getId() + " y " + venta.getLineaVenta().size() + " lineas");
			return "ventas/ventaDetails";
		}
	}

	@GetMapping(value = {
		"/ventas/actual"
	})
	public String showVentaActual(final ModelMap model) {
		Producto producto = new Producto();
		//String codigo = null;
		//model.put("command", "");
		model.put("producto", producto);
		VentaController.log.info("Se ha mostrado la venta actual");
		return "ventas/ventaActual";
	}

	@PostMapping(value = {
		"/ventas/actual"
	})
	public String ventaProcessCreation(@ModelAttribute("producto") Producto producto, @ModelAttribute("nuevaLinea") LineaVenta linea, final BindingResult result, final ModelMap model) {
		if (result.hasErrors()) {
			System.out.println(result.getAllErrors());
			return "/ventas/ventaActual";
		} else if (producto.getCode() != null) {
			try {
				producto = this.productoService.findProductoByCode(producto.getCode());
				if (this.ventaService.existelinea(producto) != null) {
					VentaController.log.info("Se ha buscado el producto '" + producto.getCode() + "' - " + producto.getName());
					return "redirect:/ventas/actual/" + this.ventaService.existelinea(producto);
				}
				linea = this.ventaService.newLinea(producto);
				model.addAttribute("nuevaLinea", linea);
				model.addAttribute("producto", producto);
				VentaController.log.info("Se ha buscado el producto '" + producto.getCode() + "' - " + producto.getName());
				return "ventas/ventaActual";
			} catch (EntityNotFoundException ex) {
				model.addAttribute("ventaActual", this.getVentaActual());
				model.put("errorProducto", "El producto no existe");
				return "/ventas/ventaActual";
			}
		} else {
			try {
				this.ventaService.saveLinea(linea);
				model.addAttribute("producto", producto);
				VentaController.log.info("Se ha guardado la linea con el producto '" + linea.getProducto().getCode() + "' en la venta actual");
				return "ventas/ventaActual";
			} catch (LineaVentaStockException ex) {
				model.addAttribute("ventaActual", this.getVentaActual());
				result.rejectValue("Cantidad", "StockInsuficiente");
				model.put("error", true);
				return "/ventas/ventaActual";
			}
		}
	}

	@GetMapping(value = {
		"/ventas/actual/{lineaId}"
	})
	public String showLineaEdit(@PathVariable("lineaId") final LineaVenta linea, final ModelMap model) {
		Producto producto = new Producto();
		model.put("producto", producto);
		model.put("editaLinea", linea);
		VentaController.log.info("Se ha mostrado la linea con id " + linea.getId() + " para ser modificada");
		return "ventas/editarLinea";
	}

	@PostMapping(value = {
		"/ventas/actual/{lineaId}"
	})
	public String LineaEdit(@ModelAttribute("producto") final Producto producto, @ModelAttribute("editarLinea") final LineaVenta linea, final BindingResult result, final ModelMap model) {
		if (result.hasErrors()) {
			return "/ventas/editarLinea";
		} else if (producto.getCode() != null) {
			return this.ventaProcessCreation(producto, linea, result, model);
		} else if (linea.getCantidad() == 0) {
			this.ventaService.deleteLinea(linea);
			VentaController.log.info("La linea con id: '" + linea.getId() + "' se ha eliminado");
			return "redirect:/ventas/actual";
		} else {
			try {
				this.ventaService.saveLinea(linea);
				VentaController.log.info("La linea con id: '" + linea.getId() + "' se ha modificado");
				return "redirect:/ventas/actual";
			} catch (LineaVentaStockException ex) {
				model.addAttribute("ventaActual", this.getVentaActual());
				model.addAttribute("editaLinea", linea);
				model.put("error", "Stock insuficiente");
				return "/ventas/editarLinea";
			}
		}
	}

	@GetMapping(value = {
		"/ventas/actual/pagar"
	})
	public String finalizarVenta(final ModelMap model) {
		Comprador comprador = new Comprador();
		Venta venta = this.ventaService.ventaActual();
		Boolean existeEstupe = false;
		for (LineaVenta linea : venta.getLineaVenta()) {
			if (linea.getProducto().getProductType() == TipoProducto.ESTUPEFACIENTE) {
				existeEstupe = true;
			}
		}
		model.addAttribute("estupefaciente", existeEstupe);
		model.addAttribute("comprador", comprador);
		VentaController.log.info("Se procede al pago de la venta actual");
		return "ventas/finalizarVenta";
	}

	@PostMapping(value = {
		"/ventas/actual/pagar"
	})
	public String createVenta(@Valid final Venta venta, @ModelAttribute("comprador") final Comprador comprador, final BindingResult result, final ModelMap model) {
		if (result.hasErrors()) {
			return "ventas/finalizarVenta";
		} else if (comprador.getDni() != null) {
			if (comprador.getDni() == "") {
				model.put("estupefaciente", true);
				VentaController.log.warn("No se ha introducido correctamente los datos del comprador estupefaciente");
				return "ventas/finalizarVenta";
			} else {
				this.ventaService.saveComprador(comprador);
				model.put("estupefaciente", false);
				VentaController.log.info("Se ha registrado el comprador estupefaceinte con DNI '" + comprador.getDni() + "'");
				return "ventas/finalizarVenta";
			}
		} else if (venta.getPagado() < venta.getImporteTotal()) {
			this.ventaService.updateVenta(venta);
			return "redirect:/ventas/actual/cliente";
		} else {
			this.ventaService.finalizarVenta(venta);
			VentaController.log.info("Se ha completado la venta satisfactoriamente");
			return "redirect:/ventas/actual";
		}
	}

	@GetMapping(value = {
		"/ventas/actual/cliente"
	})
	public String debeCliente(final ModelMap model) {
		Cliente cliente = new Cliente();
		model.put("cliente", cliente);
		VentaController.log.info("la venta debe asignarse a un cliente");
		return "ventas/asignarCliente";
	}

	@PostMapping(value = {
		"/ventas/actual/cliente"
	})
	public String AsignarCliente(@ModelAttribute("cliente") Cliente cliente, final BindingResult result, final ModelMap model) {
		if (cliente.getDni() != null) {
			try {
				cliente = this.clienteService.clienteDni(cliente.getDni());
			} catch (EntityNotFoundException ex) {
				result.rejectValue("dni", "clienteNotFound");
				return "ventas/asignarCliente";
			}
			model.put("cliente", cliente);
			VentaController.log.info("Se ha buscado un cliente con DNI '" + cliente.getDni() + "'");
			return "ventas/asignarCliente";
		} else {
			this.ventaService.asignarCliente(cliente.getId());
			VentaController.log.info("Se ha asignado el cliente satisfactoriamente");
			return "redirect:/ventas/actual";
		}
	}
}