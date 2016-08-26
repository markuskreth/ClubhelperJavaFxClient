package de.kreth.clubhelperclient.action;

import java.io.Serializable;

import de.kreth.clubhelperbackend.pojo.Data;

public abstract class Action<T extends Data> implements Serializable {

	private static final long serialVersionUID = -6946224255246283478L;

	private final T original;
	private final T changed;

	public Action(T original, T changed) {
		super();
		this.original = original;
		this.changed = changed;
	}

	public T getChanged() {
		return changed;
	}

	public T getOriginal() {
		return original;
	}

	/**
	 * Reverts this performed Action.
	 */
	public abstract void revert();

}
