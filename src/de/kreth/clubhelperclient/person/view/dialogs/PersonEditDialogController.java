package de.kreth.clubhelperclient.person.view.dialogs;

import java.net.URL;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

import org.controlsfx.control.action.Action;

import de.kreth.clubhelperbackend.pojo.Group;
import de.kreth.clubhelperbackend.pojo.Person;
import de.kreth.clubhelperclient.core.FXMLController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.ListSpinnerValueFactory;
import javafx.scene.control.TextField;

public class PersonEditDialogController extends FXMLController {

	@FXML
	protected TextField prename;

	@FXML
	protected TextField surname;

	@FXML
	protected DatePicker birth;

	@FXML
	protected Spinner<Group> groupSpinner;

	private PersonCreated handler;
	ListSpinnerValueFactory<Group> factory;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		log.debug("In initialize: " + prename);

		if (factory != null) {
			groupSpinner.setValueFactory(factory);
		}
	}

	@FXML
	private void initialize() {
		log.debug("In FXML initialize: " + prename);

		if (factory != null) {
			groupSpinner.setValueFactory(factory);
		}
	}

	@Override
	public void setFxmlFilePath(String filePath) {
	}

	public void createAndStorePerson(Action a) {
		String prenameText = prename.getText();
		String surnameText = surname.getText();
		Date birthday = Date.from(birth.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
		Group value = groupSpinner.getValue();

		Person p = new Person(1L, prenameText, surnameText, value.getName(), birthday, null, null);
		if (handler != null) {
			handler.setPerson(p);
		}
	}

	public void setGroups(ObservableMap<Long, Group> allGroups) {
		factory = new ListSpinnerValueFactory<>(FXCollections.observableArrayList(allGroups.values()));
		if (groupSpinner != null) {
			groupSpinner.setValueFactory(factory);
		}
	}

	public void setHandler(PersonCreated handler) {
		this.handler = handler;
	}

	public interface PersonCreated {
		void setPerson(Person p);
	}
}
