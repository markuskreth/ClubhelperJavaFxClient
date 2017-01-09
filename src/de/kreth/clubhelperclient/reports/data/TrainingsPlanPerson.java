package de.kreth.clubhelperclient.reports.data;

import java.util.Date;

import de.kreth.clubhelperbackend.pojo.Person;

public class TrainingsPlanPerson extends ListPerson {

	private static final long serialVersionUID = 5606242124410851834L;
	private Date beimTrampolin;
	private String telefon;

	public TrainingsPlanPerson(Person p, Date beimTrampolin, String telefon) {
		super(p);
		this.beimTrampolin = beimTrampolin;
		this.telefon = telefon;
	}

	public Date getBeimTrampolin() {
		return beimTrampolin;
	}

	public String getTelefon() {
		return telefon;
	}

}