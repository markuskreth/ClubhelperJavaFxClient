package de.kreth.clubhelperclient.person.model;

import de.kreth.clubhelperbackend.pojo.Person;
import de.kreth.clubhelperclient.Repository;

public class PersonRepository extends Repository<Person> {

	public PersonRepository() {
		super(Person.class, Person[].class);
	}

}
