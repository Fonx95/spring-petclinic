
package org.springframework.samples.farmatic.service;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.farmatic.model.Producto;
import org.springframework.samples.farmatic.model.TipoProducto;
import org.springframework.samples.farmatic.repository.ProductoRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductoService {

	private ProductoRepository productoRepository;


	@Transactional
	public Iterable<Producto> findProducts() throws DataAccessException {
		//lista productos

		return this.productoRepository.findAll();

	}

	@Transactional
	public Producto findProductoById(final int id) throws DataAccessException {
		//detalles productos
		return this.productoRepository.findById(id);
	}

	@Autowired
	public ProductoService(final ProductoRepository productoRepository) {
		this.productoRepository = productoRepository;
	}

	@Transactional
	public void saveProducto(final Producto product) throws DataAccessException {
		//creating product
		this.productoRepository.save(product);
	}
	
	@Transactional(readOnly = true)
	public Collection<TipoProducto> getProductTypes() throws DataAccessException {
		Collection<TipoProducto> productTypes = new ArrayList<TipoProducto>();
		productTypes.add(TipoProducto.ESTUPEFACIENTE);
		productTypes.add(TipoProducto.FARMACOCONRECETA);
		productTypes.add(TipoProducto.FARMACOSINRECETA);
		productTypes.add(TipoProducto.PARAFARMACIA);
		
		return productTypes;
		
	}

}
