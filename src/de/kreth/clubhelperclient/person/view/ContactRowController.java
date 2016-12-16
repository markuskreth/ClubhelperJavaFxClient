package de.kreth.clubhelperclient.person.view;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import de.kreth.clubhelperbackend.pojo.Contact;
import de.kreth.clubhelperclient.person.model.ContactRepository;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ContactRowController {

	@FXML
	Label contactLabel;
	
	@FXML
	TextField contactValue;
	
	@FXML
	Button delContactBtn;
	
	private ContactDeleted deleteAction = null;
	
	private Contact contact;

	private ExecutorService background;

	private ContactRepository contactRepository;

	private String currentTelephone;

	private Logger log;
	
	public ContactRowController(Contact c, ExecutorService background, ContactRepository contactRepository) {
		this.contact = c;
		this.background= background;
		this.contactRepository = contactRepository;
		this.log = Logger.getLogger(getClass());
	}
	
	public void setDeleteAction(ContactDeleted deleteAction) {
		this.deleteAction = deleteAction;
	}
	
	public void init() {

		updateValues();
		
		contactValue.textProperty().addListener((observable, oldValue, newValue) -> {
			
			log.info(contact + " changed from " + oldValue + " to " + newValue);
			contact.setValue(newValue);
			contact.setChanged(new Date());
			background.execute(new Runnable() {

				@Override
				public void run() {
					try {
						contactRepository.update(contact);
						contact.setValue(newValue);
						contact.setChanged(new Date());
					} catch (IOException e) {
						log.error("Update failed on " + contact, e);
					}
				}
			});
		});

		delContactBtn.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				background.execute(new Runnable() {

					@Override
					public void run() {
						try {
							contactRepository.delete(contact);
							if(deleteAction != null) {
								deleteAction.deleted(contact, delContactBtn.getParent());
							}
						} catch (IOException e) {
							log.warn("Konnte nicht löschen: " + contact, e);
						}
					}
				});
			}
		});
	}
	
	private void updateValues() {

		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

		String type2 = contact.getType();

		String lastType = null;
		currentTelephone = contact.getValue();
		
		if (lastType == null) {
			try {
				PhoneNumber parsed = phoneUtil.parse(contact.getValue(), "DE");
				currentTelephone = phoneUtil.format(parsed, PhoneNumberFormat.NATIONAL);
				lastType = type2;
			} catch (NumberParseException e) {
				log.debug("No phoneNumber: " .equals(currentTelephone));
			}
		} 

		contactLabel.setText(contact.getType());
		contactValue.setText(currentTelephone);
	}

	public interface ContactDeleted {
		void deleted(Contact c, Parent rowItem);
	}
	
}
