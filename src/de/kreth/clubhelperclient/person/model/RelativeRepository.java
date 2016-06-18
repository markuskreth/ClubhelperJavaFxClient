package de.kreth.clubhelperclient.person.model;

import org.springframework.stereotype.Component;

import de.kreth.clubhelperbackend.pojo.Relative;
import de.kreth.clubhelperclient.Repository;

@Component
public class RelativeRepository extends Repository<Relative> {

	public RelativeRepository() {
		super(Relative.class, Relative[].class);
	}

}
