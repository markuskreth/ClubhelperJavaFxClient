package de.kreth.clubhelperclient.person.model;

import de.kreth.clubhelperbackend.pojo.Contact;
import de.kreth.clubhelperclient.Repository;

public class ContactRepository extends Repository<Contact> {

	public ContactRepository() {
		super(Contact.class, Contact[].class);
	}

}
