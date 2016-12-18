package de.kreth.clubhelperclient.action;

import java.io.Serializable;

import org.apache.log4j.Logger;

import de.kreth.clubhelperbackend.pojo.Data;

public abstract class Action<T extends Data> implements Serializable {

	private static final long serialVersionUID = -6946224255246283478L;

	private final T original;
	private final T changed;

	protected Logger log;

	public Action(T original, T changed) {
		super();
		this.original = original;
		this.changed = changed;
		this.log = Logger.getLogger(getClass());
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
