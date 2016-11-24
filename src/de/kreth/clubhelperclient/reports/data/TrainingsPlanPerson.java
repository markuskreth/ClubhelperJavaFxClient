package de.kreth.clubhelperclient.reports.data;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.kreth.clubhelperbackend.pojo.Person;

public class TrainingsPlanPerson extends Person {

	private static final long serialVersionUID = 5606242124410851834L;
	private Date beimTrampolin;
	private String telefon;

	public TrainingsPlanPerson(Person p, Date beimTrampolin, String telefon) {
		super(p.getId(), p.getPrename(), p.getSurname(), p.getType(), p.getBirth(), p.getChanged(), p.getCreated());
		this.beimTrampolin = beimTrampolin;
		this.telefon = telefon;
	}

	public Date getBeimTrampolin() {
		return beimTrampolin;
	}

	public String getTelefon() {
		return telefon;
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

}