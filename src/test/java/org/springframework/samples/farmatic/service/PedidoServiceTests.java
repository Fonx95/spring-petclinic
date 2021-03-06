
package org.springframework.samples.farmatic.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.samples.farmatic.model.LineaPedido;
import org.springframework.samples.farmatic.model.Pedido;
import org.springframework.samples.farmatic.model.Pedido.EstadoPedido;
import org.springframework.samples.farmatic.model.Proveedor;
import org.springframework.samples.farmatic.repository.ClienteRepository;
import org.springframework.samples.farmatic.repository.LineaPedidoRepository;
import org.springframework.samples.farmatic.repository.ProductoRepository;
import org.springframework.samples.farmatic.repository.ProveedorRepository;
import org.springframework.samples.farmatic.service.exception.EstadoPedidoException;
import org.springframework.samples.farmatic.service.exception.LineaEmptyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest(includeFilters = @ComponentScan.Filter(Service.class))
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class PedidoServiceTests {

	@Autowired
	protected PedidoService			pedidoService;

	@Autowired
	protected LineaPedidoRepository	lineaRepository;

	@Autowired
	protected ProveedorRepository	proveedorRepository;

	@Autowired
	protected ProductoRepository	productoRepository;

	@Autowired
	protected ClienteRepository		clienteRepository;

	// Recordatorio: No hay un create directo en los pedidos, sino que se crean al usar enviarPedido, por lo tanto, en ese test se comprobará la creación.


	//Test positivos

	@Test
	public void getPedidoActual() { // Método que devuelve el pedido actual auto creado por el sistema.
		Pedido p = this.pedidoService.pedidoActual();
		Assertions.assertNotNull(p); // Comprobamos que no sea nulo.
		Assertions.assertTrue(p.getEstadoPedido().equals(EstadoPedido.Borrador)); // Comprobamos que está en estado Borrador.
	}

	@Test
	public void shouldFindAllPedidos() {// Método que comprueba que se listan los pedidos
		Collection<Pedido> pedidos = this.pedidoService.findPedidos();
		Pedido[] pedidosArr = pedidos.toArray(new Pedido[pedidos.size()]);
		Collection<LineaPedido> lineasP1 = pedidosArr[1].getLineaPedido();
		Assertions.assertTrue(pedidos.size() == 6);// Se comprueba el numero de pedidos recibidos pomr la base de datos
		Assertions.assertTrue(lineasP1.size() == 2);// Se comprueba que uno de los pedidos (que no sea el borrador) tiene lineas de pedidos asignadas
	}

	@Test
	public void shouldFindPedidoById() {// Metodo que comprueba los detalles de un pedido
		Pedido pedido = this.pedidoService.pedido(2);
		Collection<LineaPedido> lineasP1 = pedido.getLineaPedido();
		Assertions.assertTrue(pedido.getCodigo().equals("P-005"));// Comprueba que el codigo es el esperado
		Assertions.assertTrue(pedido.getEstadoPedido().equals(EstadoPedido.Enviado));// Comprueba que el estado es el esperado
		Assertions.assertNotNull(pedido.getFechaPedido());// Comprueba que la fecha del pedido esta asignada
		Assertions.assertTrue(lineasP1.size() == 3);// Comprueba que tiene lineas de pedidos asignadas
	}

	@Test
	@Transactional
	public void shouldInsertLineaPedido() {
		LineaPedido lp = this.pedidoService.newLinea(this.productoRepository.findById(1), 1); // Generamos una nueva línea de pedido con el servicio, esto le asigna el pedido actual y el producto que le pasemos.

		Assertions.assertNotNull(lp); // Comprobamos que no es nula.

		this.pedidoService.saveLinea(lp); // Guardamos la linea.
		Iterable<LineaPedido> lp2 = this.lineaRepository.findAll();
		Collection<LineaPedido> lps = new ArrayList<>();
		lp2.iterator().forEachRemaining(x -> lps.add(x)); // Todo esto es requerido para obtener la id que tiene la linea puesto que cambia sola al guardarse.

		Assertions.assertTrue(this.lineaRepository.findById(lps.size()).get().equals(lp));
	}

	@Test
	@Transactional
	public void pedirPedidoPositivo() { // Modificamos un pedido de Borrador a Pedido.
		Proveedor prov = this.proveedorRepository.findById(1);
		Pedido p = this.pedidoService.pedidoActual(); // Nos traemos el pedido actual para comprobar que se realizan las modificaciones.

		try {
			this.pedidoService.enviarPedido(prov); // Función que cambia el estado de Borrador a Pedido, pone la nueva fecha de pedido y asigna el proveedor al que se pide.
		} catch (LineaEmptyException ex) {
			ex.printStackTrace();//el pedido no tiene lineas de pedido
		}

		Pedido p1 = this.pedidoService.pedido(p.getId());
		Assertions.assertTrue(p1.getProveedor().equals(prov));
		Assertions.assertTrue(p1.getFechaPedido().equals(LocalDate.now()));
		Assertions.assertTrue(p1.getEstadoPedido() == EstadoPedido.Pedido);

		Pedido p2 = this.pedidoService.pedidoActual(); // La función anterior también crea un pedido nuevo en estado borrador.
		Assertions.assertNotNull(p2);
		Assertions.assertTrue(p2.getEstadoPedido() == EstadoPedido.Borrador);
	}

	@Test
	@Transactional
	public void recibirPedidoPositivo() { // Modificamos un pedido de Enviado a Recibido.
		Pedido p = this.pedidoService.pedido(2); // Nos traemos el pedido con estado Enviado de la BD.

		List<Integer> cantidadLp = new ArrayList<>();
		List<Integer> stockOriginal = new ArrayList<>();
		List<Integer> stockActual = new ArrayList<>();
		p.getLineaPedido().stream().forEach(x -> cantidadLp.add(x.getCantidad()));
		p.getLineaPedido().stream().forEach(x -> stockOriginal.add(x.getProducto().getStock()));

		try {
			this.pedidoService.recibirPedido(p); // Función que cambia el estado de Enviado a Recibido, pone fecha de entrega y suma las cantidades de producto.
		} catch (EstadoPedidoException ex) {
			ex.printStackTrace();//el pedido no tiene el estado adecuado
		}
		Pedido p1 = this.pedidoService.pedido(p.getId());
		Assertions.assertTrue(p1.getFechaEntrega().equals(LocalDate.now()));
		Assertions.assertTrue(p1.getEstadoPedido() == EstadoPedido.Recibido);

		p1.getLineaPedido().stream().forEach(x -> stockActual.add(x.getProducto().getStock()));

		int i = 0;
		while (i < cantidadLp.size()) {
			Assertions.assertTrue(stockActual.get(i) == stockOriginal.get(i) + cantidadLp.get(i)); // Comprobamos que el stock se suma correctamente.
			i++;
		}
	}

	@Test
	@Transactional
	public void enviarPedidoPositivo() {// Metodo que comprueba que un pedido cambia de estado a enviado por el proveedor
		Pedido p = this.pedidoService.pedido(3);// Nos traemos un pedido en estado pedido de la BD

		try {
			this.pedidoService.pedidoEnviado(p);
		} catch (EstadoPedidoException ex) {
			ex.printStackTrace();//el pedido no tiene el estado adecuado
		}

		Pedido p1 = this.pedidoService.pedido(p.getId());
		Assertions.assertTrue(p1.getEstadoPedido() == EstadoPedido.Enviado);// Comprobamos que el estado se ha cambiado a estado enviado
	}

	//Test negativos

	// Test shouldNotInsertLineaPedido, la función es permisiva por su uso en otras funciones.

	@Test
	@Transactional
	public void pedirPedidoNegativo() { // Probamos a mandar un pedido sin lineas de pedido
		Pedido p = this.pedidoService.pedidoActual();
		Proveedor prov = this.proveedorRepository.findById(1);
		try {
			this.pedidoService.enviarPedido(prov);
		} catch (LineaEmptyException ex) {
			ex.printStackTrace();
		}
		Assertions.assertThrows(LineaEmptyException.class, () -> this.pedidoService.enviarPedido(null)); // Comprobamos si salta la excepción adecuada.
	}

	@Test
	@Transactional
	public void recibirPedidoNegativo() { // Probamos a mandar un pedido recien creado.
		Pedido p = new Pedido(); // Creamos un nuevo pedido.
		Assertions.assertThrows(Exception.class, () -> this.pedidoService.recibirPedido(p));
	}

	@Test
	@Transactional
	public void enviarPedidoNegativo() { // Probamos a mandar un pedido recien creado
		Pedido p = new Pedido();// Creamos un pedido
		Assertions.assertThrows(NullPointerException.class, () -> {
			this.pedidoService.pedidoEnviado(p);
		});// Comprobamos que se obtiene un nullPointerException
	}
}
