package de.kreth.clubhelperclient.action;

import java.io.IOException;

import de.kreth.clubhelperbackend.pojo.Data;
import de.kreth.clubhelperclient.Repository;

public class RepositoryDeleteAction<T extends Data> extends Action<T> {

	private static final long serialVersionUID = 145974066915287119L;
	private Repository<T> repository;

	public RepositoryDeleteAction(T original, T changed, Repository<T> repository) {
		super(original, changed);
		this.repository = repository;
	}

	@Override
	public void revert() {
		try {
			repository.insert(getOriginal());
		} catch (IOException e) {
			log.error("unable to revert delete of " + getOriginal(), e);
		}
	}

}
