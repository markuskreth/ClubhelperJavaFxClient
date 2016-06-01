package de.kreth.clubhelperclient.person.model;

import de.kreth.clubhelperbackend.pojo.Relative;
import de.kreth.clubhelperclient.Repository;

public class RelativeRepository extends Repository<Relative> {

	public RelativeRepository() {
		super(Relative.class, Relative[].class);
	}

}
