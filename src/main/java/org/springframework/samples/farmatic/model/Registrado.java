/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.farmatic.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotEmpty;

import lombok.Data;

/**
 * Simple JavaBean domain object representing an person.
 *
 * @author Ken Krebs
 */
@Data
@MappedSuperclass
public class Registrado extends NamedEntity {

	
	@NotEmpty
	@Column(name = "surnames")
	private String surnames;

	
	@NotEmpty
	@Column(name = "dni")
	private String dni;
	
	@OneToOne(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinColumn(name = "username", referencedColumnName = "username")
	private User user;

	

}
