package de.kreth.clubhelperclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;

import de.kreth.clubhelperbackend.pojo.Person;
import de.kreth.clubhelperclient.action.Action;

public class EditActionTest {

	private Person original;

	@Before
	public void setUp() {

		Date birth = new GregorianCalendar(1973, Calendar.AUGUST, 21).getTime();
		Date changed = new Date();
		Date created = changed;

		original = new Person(1L, "Markus", "Kreth", "Test", birth, changed, created);

	}

	@Test
	public void createActions() throws CloneNotSupportedException {
		Person changed = (Person) original.clone();
		changed.setSurname("Kreth2");
		Calendar newChanged = new GregorianCalendar();
		newChanged.setTime(changed.getChanged());
		newChanged.add(Calendar.MINUTE, 2);

		changed.setChanged(newChanged.getTime());
		assertFalse(changed.equals(original));

		Action<Person> action = new Action<Person>(original, changed) {

			private static final long serialVersionUID = 1L;

			@Override
			public void revert() {

			}

		};

		assertEquals(original, action.getOriginal());
		assertEquals(changed, action.getChanged());

	}
}
