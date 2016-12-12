package de.kreth.clubhelperclient.person.view.dialogs;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import de.kreth.clubhelperbackend.pojo.Group;
import de.kreth.clubhelperbackend.pojo.Person;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.ListSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class PersonEditDialogController {

	private Stage stage;

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

	@FXML
	public void onOk() {
		createAndStorePerson();
	}

	@FXML
	public void onCancel() {
		stage.close();
	}

	public void createAndStorePerson() {
		String prenameText = prename.getText();
		String surnameText = surname.getText();
		LocalDate birthValue = birth.getValue();
		Date birthday = birthValue != null ? Date.from(birthValue.atStartOfDay(ZoneId.systemDefault()).toInstant())
				: null;
		Group value = groupSpinner.getValue();

		Person p = new Person(-1L, prenameText, surnameText, value.getName(), birthday, null, null);
		if (handler != null) {
			handler.setPerson(p);
		}
		onCancel();
	}

	public void setGroups(ObservableMap<Long, Group> allGroups) {
		factory = new ListSpinnerValueFactory<>(FXCollections.observableArrayList(allGroups.values()));
		factory.setConverter(new StringConverter<Group>() {

			@Override
			public String toString(Group object) {
				return object.getName();
			}

			@Override
			public Group fromString(String string) {
				for (Group g : factory.getItems()) {
					if (g.getName().equals(string)) {
						return g;
					}

				}

				return new Group(-1L, string, null, null);
			}
		});

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

	public void setStage(Stage stage) {
		this.stage = stage;
	}
}
