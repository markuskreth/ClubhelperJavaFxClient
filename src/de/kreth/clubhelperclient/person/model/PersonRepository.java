package de.kreth.clubhelperclient.person.model;

import org.springframework.stereotype.Component;

import de.kreth.clubhelperbackend.pojo.Person;
import de.kreth.clubhelperclient.Repository;

@Component
public class PersonRepository extends Repository<Person> {

	public PersonRepository() {
		super(Person.class, Person[].class);
	}

}
