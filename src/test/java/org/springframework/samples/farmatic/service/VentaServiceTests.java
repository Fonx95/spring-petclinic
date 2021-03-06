package org.springframework.samples.farmatic.service;


import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.Collection;

import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.farmatic.model.Cliente;
import org.springframework.samples.farmatic.model.LineaVenta;
import org.springframework.samples.farmatic.model.TipoTasa;
import org.springframework.samples.farmatic.model.Venta;
import org.springframework.samples.farmatic.model.Venta.EstadoVenta;
import org.springframework.samples.farmatic.model.validator.LineaVentaValidator;
import org.springframework.samples.farmatic.model.Producto;
import org.springframework.samples.farmatic.repository.ClienteRepository;
import org.springframework.samples.farmatic.repository.LineaVentaRepository;
import org.springframework.samples.farmatic.repository.ProductoRepository;
import org.springframework.samples.farmatic.service.exception.LineaEmptyException;
import org.springframework.samples.farmatic.service.exception.VentaClienteEmptyException;
import org.springframework.samples.farmatic.service.exception.VentaCompradorEmptyException;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest(includeFilters = @ComponentScan.Filter(Service.class))
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class VentaServiceTests {
	

	@Autowired
	protected VentaService ventaService;

	@Autowired
	protected LineaVentaRepository	lineaRepository;

	@Autowired
	protected ProductoRepository	productoRepository;
	
	@Autowired
	protected ClienteRepository clienteRepository;
	
	//Test positivo
	
	@Test
	public void getVentaActual() { // Método que devuelve la venta actual creada automaticamente por el sistema
		Venta v = this.ventaService.ventaActual();
		Assertions.assertNotNull(v); // vamos a comprobar que no sea nulo.
		Assertions.assertTrue(v.getEstadoVenta().equals(EstadoVenta.enProceso)); // Comprobamos que está en proceso.
	}
	
	@Test
	public void shouldFindAllVentas() {// Método que comprueba el correcto listado de las ventas
		Collection<Venta> ventas = this.ventaService.findAllVentas();
		Venta[] ventasArr = ventas.toArray(new Venta[ventas.size()]);
		Collection<LineaVenta> lineasV1 = ventasArr[1].getLineaVenta();
		Assertions.assertTrue(ventas.size() == 6);// Se comprueba el numero de ventas recibidas por la base de datos
		Assertions.assertTrue(lineasV1.size() == 3);// Comprobamos que una de las ventas (que no sea la que esta en proceso) tiene lineas de ventas asignadas
	}
	
	@Test
	public void shouldFindVentaById() {// Metodo que comprueba los detalles de la venta
		Venta venta = this.ventaService.venta(2);
		Collection<LineaVenta> lineasV1 = venta.getLineaVenta();
		Assertions.assertTrue(venta.getEstadoVenta().equals(EstadoVenta.Realizada));// Comprueba que tenemos el estado que queremos
		Assertions.assertNotNull(venta.getFecha());// Comprueba que se ha asignado la fecha de la venta
		Assertions.assertTrue(lineasV1.size() == 2);// Comprueba que tiene lineas de ventas asignadas
	}
	
	@Test
	@Transactional
	public void shouldInsertLineaVenta() {
		LineaVenta lv = this.ventaService.newLinea(this.productoRepository.findById(1)); // vamos a generar una nueva línea de venta usando el servicio
		lv.setTipoTasa(TipoTasa.TSI001);
		lv.setCantidad(1);

		Assertions.assertNotNull(lv); // Comprobamos que no es nula.
		
		this.ventaService.saveLinea(lv); // Guardamos la linea.
		Integer linea_id = lv.getId(); //obtenemos el id de la linea.
		Assertions.assertTrue(this.lineaRepository.findById(linea_id).get().equals(lv));
	}
	
	@Test
	@Transactional
	public void finalizarVentaPositivo() throws DataAccessException, VentaCompradorEmptyException, VentaClienteEmptyException, LineaEmptyException { // Actualizamos una venta de en proceso a finalizada.
		Venta v = this.ventaService.ventaActual(); // cogemos la venta actual para comprobar que se han modificado los atributos
		Assertions.assertTrue(v.getEstadoVenta() == EstadoVenta.enProceso);
		LineaVenta lv = this.ventaService.newLinea(this.productoRepository.findById(1));
		lv.setCantidad(1);
		lv.setTipoTasa(TipoTasa.TSI001);
		this.ventaService.saveLinea(lv);
		
		v.addLinea(lv);
		v.setCliente(this.clienteRepository.findById(1));
		
		try {
			this.ventaService.finalizarVenta(v); // Función que cambia el estado de en proceso a finalizada y pone la nueva fecha de venta.
		}catch(LineaEmptyException ex){
			ex.printStackTrace();//la venta no tiene lineas de venta
		}
    
		Venta v1 = this.ventaService.venta(v.getId());
		Assertions.assertTrue(v1.getFecha().equals(LocalDate.now()));
		Assertions.assertTrue(v1.getEstadoVenta() == EstadoVenta.Realizada);
		
		Venta v2 = this.ventaService.ventaActual(); // La función anterior también crea una venta nueva.
		Assertions.assertNotNull(v2);
		Assertions.assertTrue(v2.getEstadoVenta() == EstadoVenta.enProceso);

		
	}
	
	@Test
	@Transactional
	public void updateVentaPositivo() {//comprobamos si la venta se actualiza correctamente
		Venta v = this.ventaService.venta(2);
		Double importe_inicial = v.getImporteTotal(); //establecemos los parametros iniciales
		Double porPagar_inicial = v.getPorPagar();
		this.ventaService.updateVenta(v);
		Double importe_2 = v.getImporteTotal(); //establecemos los parametros actualizados
		Double porPagar_2 = v.getPorPagar();
		v.addLinea(this.lineaRepository.lineaVenta(1));
		this.ventaService.updateVenta(v);
		Assertions.assertTrue(v.getImporteTotal() != importe_inicial && v.getImporteTotal() != importe_2); //comprobamos si los parametros se han actualizado
		Assertions.assertTrue(v.getPorPagar() != porPagar_inicial && v.getPorPagar() != porPagar_2);
		
		
	}
	
	//Test negativos
	
	@Test
	@Transactional
	public void shouldNotInsertLineaVenta() { // No podemos guardar porque directamente no podemos crear con el método usado por el sistema.
		try {
			LineaVenta lv = this.ventaService.newLinea(this.productoRepository.findById(0));
		} catch (Exception e) {
			Assertions.assertNotNull(e);
		}
	}
	
	@Test
	void shouldNotFindVentaByID() {
		assertThrows(NullPointerException.class, ()->{this.ventaService.venta(0).getClass();}); // Comprobará que se lanza un NullPointerException al realizar la acción en el corchete.
	}
	
	@Test
	@Transactional
	public void finalizarVentaNegativo() throws DataAccessException, VentaCompradorEmptyException, VentaClienteEmptyException {//Probamos a intentar finalizar una venta recien creada sin lineas de venta
		Venta v = this.ventaService.ventaActual();
		try {
			this.ventaService.finalizarVenta(v);
		} catch (LineaEmptyException ex) {
			ex.printStackTrace();
		}
		Assertions.assertThrows(LineaEmptyException.class, () -> this.ventaService.finalizarVenta(v)); // Comprobamos si salta la excepción adecuada.
		
	}
	
	@Test
	@Transactional
	public void shouldNotInsertLineaStock() {//No podemos insertar una linea de venta porque la cantidad supera al stock
		Producto p = this.productoRepository.findById(1);
		LineaVenta lv = this.ventaService.newLinea(p);
		p.setStock(3);
		try {
			lv.setCantidad(4);
		}catch(Exception e) {
			Assertions.assertNotNull(e);
		}
		
	}

}
