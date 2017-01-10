package de.kreth.clubhelperclient.reports.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import de.kreth.clubhelperbackend.pojo.Contact;
import de.kreth.clubhelperbackend.pojo.Person;
import de.kreth.clubhelperbackend.pojo.Relative;
import de.kreth.clubhelperclient.person.model.ContactRepository;
import de.kreth.clubhelperclient.person.model.RelativeRepository;

public class PhoneListPerson extends ListPerson {
	
	private static final long serialVersionUID = 3576869933198308973L;

	protected static PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
	
	protected Logger log = Logger.getLogger(getClass());
	private final List<Contact> phones;
	private final List<PhoneListPerson> relatives;

	public PhoneListPerson(Person p) {
		super(p.getId(), p.getPrename(), p.getSurname(), p.getType(), p.getBirth(), p.getChanged(), p.getCreated());
		phones = new ArrayList<>();
		relatives = new ArrayList<>();
	}

	public PhoneListPerson(Person p, List<Contact> phones) {
		this(p);
		if (phones != null)  {
			this.phones.addAll(phones);
		}
	}

	public PhoneListPerson(Person p, List<Contact> phones, List<PhoneListPerson> relatives) {
		this(p, phones);
		if (relatives != null)  {
			 this.relatives.addAll(relatives);
		}
	}

	public List<String> getContacts() {

		
		List<String> result = new ArrayList<>();
		for (Contact c: phones) {
			String value = c.getValue();

			value = parsePhoneNumber(value);
			result.add(c.getType() + "=" + value);
		}
		for (PhoneListPerson p: relatives) {

			for (Contact c: p.phones) {
				String value = c.getValue();

				value = parsePhoneNumber(value);
				result.add(p.getPrename() + " " + p.getSurname() + ": " + c.getType() + "=" + value);
			}
			
		}
		return result;
	}

	public String getContactString() {
		return String.join("; ", getContacts());
	}
	
	private String parsePhoneNumber(String value) {
		try {
			PhoneNumber parsed = phoneUtil.parse(value, "DE");
			value = phoneUtil.format(parsed, PhoneNumberFormat.NATIONAL);
		} catch (NumberParseException e) {
			log.trace("No phoneNumber: " .equals(value));
		}
		return value;
	}
		
	public static PhoneListPerson createAndFill(Person p, List<Person> personData, RelativeRepository relativeRepository, ContactRepository contactRepository) throws InterruptedException, ExecutionException {
		final List<Contact> contacts = new ArrayList<>();
		final List<PhoneListPerson> relatives = new ArrayList<>();
		ExecutorService exec = Executors.newSingleThreadExecutor();
		Future<Boolean> submit = exec.submit(new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {

				contacts.addAll(contactRepository.getByParentId(p.getId()).stream()
						.filter(con -> con.getType().toLowerCase().contains("mail") == false)
						.collect(Collectors.toList()));
				List<Relative> rels = relativeRepository.getByParentId(p.getId());
				for (Relative r : rels) {
					Person relative = personData.stream()
							.filter(potRel -> potRel.getId().equals(p.getId()) == false
									&& (r.getPerson1() == potRel.getId().longValue()
											|| r.getPerson2() == potRel.getId().longValue()))
							.findFirst().orElse(null);
					final List<Contact> rCon = new ArrayList<>(contactRepository.getByParentId(relative.getId())
							.stream().filter(con -> con.getType().toLowerCase().contains("mail") == false)
							.collect(Collectors.toList()));
					relatives.add(new PhoneListPerson(relative, rCon));
				}
				return Boolean.TRUE;
			}
		});

		submit.get();
		
		return new PhoneListPerson(p, contacts, relatives);
	}
}
