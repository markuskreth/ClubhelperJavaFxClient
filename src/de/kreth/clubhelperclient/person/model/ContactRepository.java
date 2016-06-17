package de.kreth.clubhelperclient.person.model;

import org.springframework.stereotype.Component;

import de.kreth.clubhelperbackend.pojo.Contact;
import de.kreth.clubhelperclient.Repository;

@Component
public class ContactRepository extends Repository<Contact> {

	public ContactRepository() {
		super(Contact.class, Contact[].class);
	}

}
