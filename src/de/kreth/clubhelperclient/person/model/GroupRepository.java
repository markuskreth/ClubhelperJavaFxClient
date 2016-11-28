package de.kreth.clubhelperclient.person.model;

import org.springframework.stereotype.Component;

import de.kreth.clubhelperbackend.pojo.Group;
import de.kreth.clubhelperclient.Repository;

@Component
public class GroupRepository extends Repository<Group> {

	public GroupRepository() {
		super(Group.class, Group[].class);
	}

}
