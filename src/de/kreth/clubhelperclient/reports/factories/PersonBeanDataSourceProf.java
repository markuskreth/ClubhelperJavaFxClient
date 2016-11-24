package de.kreth.clubhelperclient.reports.factories;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import de.kreth.clubhelperbackend.pojo.Person;
import de.kreth.clubhelperbackend.pojo.PersonType;
import de.kreth.clubhelperclient.reports.data.TrainingsPlanPerson;

public class PersonBeanDataSourceProf extends GenericBeanDataSourceProvider<TrainingsPlanPerson> {

	private static List<TrainingsPlanPerson> ps = new ArrayList<>();
	static {

		Date changed = new GregorianCalendar(2016, Calendar.APRIL, 5, 10, 50, 13).getTime();
		Person m = new Person(-1L, "Markus", "Kreth", PersonType.ACTIVE.toString(),
				new GregorianCalendar(1973, Calendar.AUGUST, 21).getTime(), changed, changed);

		TrainingsPlanPerson p = new TrainingsPlanPerson(m,
				new GregorianCalendar(2006, Calendar.SEPTEMBER, 5, 10, 50, 13).getTime(), "0174-2521286");

		List<TrainingsPlanPerson> persons = new ArrayList<>();
		persons.add(p);
	}

	public PersonBeanDataSourceProf() {
		super(TrainingsPlanPerson.class, ps);
	}

	public PersonBeanDataSourceProf(List<TrainingsPlanPerson> persons) {
		super(TrainingsPlanPerson.class, persons);
	}

}
