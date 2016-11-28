package de.kreth.clubhelperclient.person.model;

import org.springframework.stereotype.Component;

import de.kreth.clubhelperbackend.pojo.PersonGroup;
import de.kreth.clubhelperclient.Repository;

@Component
public class PersonGroupRepository extends Repository<PersonGroup> {

	public PersonGroupRepository() {
		super(PersonGroup.class, PersonGroup[].class);
	}

}
