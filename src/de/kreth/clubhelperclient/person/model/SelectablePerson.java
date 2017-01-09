package de.kreth.clubhelperclient.person.model;

import de.kreth.clubhelperbackend.pojo.Person;
import de.kreth.clubhelperclient.reports.data.ListPerson;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SelectablePerson extends ListPerson {

	private static final long serialVersionUID = 4386351169542041600L;
	private StringProperty name;
	private BooleanProperty selected;
	
	public SelectablePerson(Person p) {
		super(p);
		name = new SimpleStringProperty(toString());
		selected = new SimpleBooleanProperty(false);
	}
	
	public StringProperty nameProperty() {
		return name;
	}
	
	public BooleanProperty selectedProperty() {
		return selected;
	}
}
