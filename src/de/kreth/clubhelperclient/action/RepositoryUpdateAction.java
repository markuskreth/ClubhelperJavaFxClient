package de.kreth.clubhelperclient.action;

import java.io.IOException;

import de.kreth.clubhelperbackend.pojo.Data;
import de.kreth.clubhelperclient.Repository;

public class RepositoryUpdateAction<T extends Data> extends Action<T> {

	private static final long serialVersionUID = 4327398343524022536L;

	private final Repository<T> repository;

	public RepositoryUpdateAction(T original, T changed, Repository<T> repository) {
		super(original, changed);
		this.repository = repository;
	}

	@Override
	public void revert() {
		try {
			repository.update(getOriginal());
		} catch (IOException e) {
			throw new RuntimeException("Revert action failed", e);
		}
	}

}
