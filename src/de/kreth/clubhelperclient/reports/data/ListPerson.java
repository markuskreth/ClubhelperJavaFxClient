package de.kreth.clubhelperclient.reports.data;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

import de.kreth.clubhelperbackend.pojo.Person;

public class ListPerson extends Person {

	private static final long serialVersionUID = 226598896984490901L;

	public ListPerson(Long id, String prename, String surname, String type, Date birth, Date changed, Date created) {
		super(id, prename, surname, type, birth, changed, created);
	}

	public ListPerson(Person p) {
		super(p.getId(), p.getPrename(), p.getSurname(), p.getType(), p.getBirth(), p.getChanged(), p.getCreated());
	}

	public int getAlter() {
		Date birth = getBirth();
		if (birth == null) {
			return 0;
		}
	
		Date now = new Date();
		return getDiffYears(birth, now);
	}

	public int getDiffYears(Date first, Date last) {
		Calendar a = getCalendar(first);
		Calendar b = getCalendar(last);
		int diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR);
		if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH)
				|| (a.get(Calendar.MONTH) == b.get(Calendar.MONTH) && a.get(Calendar.DATE) > b.get(Calendar.DATE))) {
			diff--;
		}
		return diff;
	}

	public Calendar getCalendar(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal;
	}

	public static Comparator<Person> personComparator() {
		return new Comparator<Person>() {

			@Override
			public int compare(Person o1, Person o2) {
				int compareTo = o1.getSurname().compareTo(o2.getSurname());
				if(compareTo == 0) {
					compareTo = o1.getPrename().compareTo(o2.getPrename());
				}
				return compareTo;
			}
		};
	}

	@Override
	public String toString() {
		return getSurname() + ", " + getPrename();
	}
}