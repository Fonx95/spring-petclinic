package org.springframework.samples.farmatic.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.samples.farmatic.model.Authorities;
import org.springframework.samples.farmatic.model.Cliente;
import org.springframework.samples.farmatic.model.Producto;
import org.springframework.samples.farmatic.model.User;
import org.springframework.samples.farmatic.repository.AuthoritiesRepository;
import org.springframework.samples.farmatic.repository.ClienteRepository;
import org.springframework.samples.farmatic.repository.ProductoRepository;
import org.springframework.samples.farmatic.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {
	
	
	private ClienteRepository clienteRepo;
	
	private UserService			userService;
	
	private final AuthoritiesService	authoritiesService;
	@Transactional
	public int clienteCount() {
		return (int) clienteRepo.count();
	}
	

	@Transactional
	public Iterable<Cliente> findClientes() throws DataAccessException {
		//lista productos

		return this.clienteRepo.findAll();

	}

	@Transactional
	public Cliente findClienteById(final int id) throws DataAccessException {
	
		return this.clienteRepo.findById(id);
	}

	@Autowired
	public ClienteService(final ClienteRepository clienteRepo, final UserService userService, final AuthoritiesService	authoritiesService) {
		this.clienteRepo = clienteRepo;
		this.userService = userService;
		this.authoritiesService = authoritiesService;
	}
	
	@Transactional
	public void saveCliente(final Cliente cliente) throws DataAccessException {
		//creating cliente
		cliente.setPorPagarTotal(0.0);
		this.clienteRepo.save(cliente);
		this.userService.saveUser(cliente.getUser());
		this.authoritiesService.saveAuthorities(cliente.getUser().getUsername(), "cliente");
		
		
	}
	
	@Transactional
	public Cliente findClienteData() throws DataAccessException {
		//detalles cliente
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentPrincipalName = authentication.getName();             //Obtiene el nombre del ususario actual
		User user = this.userService.findUser(currentPrincipalName).get();         //Obtiene el usuario con ese nombre
		return this.clienteRepo.findByUser(user);
	}
}
