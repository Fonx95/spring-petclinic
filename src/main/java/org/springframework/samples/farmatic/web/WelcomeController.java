package org.springframework.samples.farmatic.web;

import java.util.Map;

import org.springframework.samples.farmatic.model.Person;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class WelcomeController {
	
	
	  @GetMapping({"/","/welcome"})
	  public String welcome(Map<String, Object> model) {	    
		  List<Person> persons = new ArrayList<Person>();
		  Person person = new Person();
		  person.setFirstName("Alfonso");
		  person.setLastName("Masa Calderon");
		  persons.add(person);
		  Person person1 = new Person();
		  person1.setFirstName("Fernando");
		  person1.setLastName("Madroñal Rodriguez");
		  persons.add(person1);
		  Person person2 = new Person();
		  person2.setFirstName("Antonio");
		  person2.setLastName("Rosado Barrera");
		  persons.add(person2);
		  Person person3 = new Person();
		  person3.setFirstName("Gabriel");
		  person3.setLastName("Gutierrez Prieto");
		  persons.add(person3);
		  Person person4 = new Person();
		  person4.setFirstName("Francisco Jose");
		  person4.setLastName("Vargas Castro");
		  persons.add(person4);
		  Person person5 = new Person();
		  person5.setFirstName("Abdelkader");
		  person5.setLastName("Chellik");
		  persons.add(person5);
		  model.put("persons", persons);
		  model.put("title", "Farmatic Proyect");
		  model.put("group", "10");
		  return "welcome";
	  }
}