package de.kreth.clubhelperclient.reports.factories;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import de.kreth.clubhelperbackend.pojo.Person;
import de.kreth.clubhelperbackend.pojo.PersonType;
import de.kreth.clubhelperclient.reports.data.TrainingsPlanPerson;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataSourceProvider;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.base.JRBaseField;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class PersonDataSourceProvider implements JRDataSourceProvider {

	private List<TrainingsPlanPerson> persons;

	public PersonDataSourceProvider() {
		persons = new ArrayList<>();

		Date changed = new GregorianCalendar(2016, Calendar.APRIL, 5, 10, 50, 13).getTime();
		Person m = new Person(-1L, "Markus", "Kreth", PersonType.ACTIVE.toString(),
				new GregorianCalendar(1973, Calendar.AUGUST, 21).getTime(), changed, changed);

		TrainingsPlanPerson p = new TrainingsPlanPerson(m,
				new GregorianCalendar(2006, Calendar.SEPTEMBER, 5, 10, 50, 13).getTime(), "0174-2521286");
		persons.add(p);
	}

	public List<TrainingsPlanPerson> getPersons() {
		return persons;
	}

	@Override
	public boolean supportsGetFieldsOperation() {
		return true;
	}

	@Override
	public JRField[] getFields(JasperReport report) throws JRException, UnsupportedOperationException {
		PersonFields vorname = new PersonFields("prename", "Vorname", String.class);
		PersonFields nachname = new PersonFields("surname", "Nachname", String.class);
		PersonFields beimTrampolin = new PersonFields("beimTrampolin", "Datum des Eintritts", Date.class);
		PersonFields birth = new PersonFields("birth", "Geburtstag", Date.class);
		PersonFields telefon = new PersonFields("telefon", "Telefon der Eltern", String.class);

		JRField[] result = { vorname, nachname, beimTrampolin, birth, telefon };
		return result;
	}

	@Override
	public JRDataSource create(JasperReport report) throws JRException {
		return new JRBeanCollectionDataSource(persons, true);
	}

	@Override
	public void dispose(JRDataSource dataSource) throws JRException {
		persons.clear();
	}

	public class PersonFields extends JRBaseField {

		private static final long serialVersionUID = 2307956343988306214L;

		public PersonFields(String fieldName, String description, Class<?> valueClass) {
			super();
			this.name = fieldName;
			this.description = description;
			this.valueClass = valueClass;
			this.valueClassName = valueClass.getName();
		}

	}
}
