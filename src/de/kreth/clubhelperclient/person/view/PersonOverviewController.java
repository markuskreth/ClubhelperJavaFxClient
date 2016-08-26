package de.kreth.clubhelperclient.person.view;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.controlsfx.dialog.ExceptionDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.kreth.clubhelperbackend.pojo.Contact;
import de.kreth.clubhelperbackend.pojo.Person;
import de.kreth.clubhelperbackend.pojo.Relative;
import de.kreth.clubhelperclient.action.RepositoryUpdateAction;
import de.kreth.clubhelperclient.core.FXMLController;
import de.kreth.clubhelperclient.person.model.ContactRepository;
import de.kreth.clubhelperclient.person.model.PersonRepository;
import de.kreth.clubhelperclient.person.model.RelativeRepository;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

@Component
public class PersonOverviewController extends FXMLController {

	private final ExecutorService background;

	public PersonOverviewController() {
		super();
		background = Executors.newCachedThreadPool();
	}

	@FXML
	private TableView<Person> tblPersonen;

	@FXML
	private TableColumn<Person, String> columnPrename;

	@FXML
	private TableColumn<Person, String> columnSurname;

	@FXML
	private TableColumn<Person, String> columnAge;

	@FXML
	private TextField detailPersonPrename;

	@FXML
	private TextField detailPersonSurname;

	@FXML
	private DatePicker detailBirthday;

	@FXML
	private Label detailAge;

	@FXML
	private Spinner<de.kreth.clubhelperbackend.pojo.Group> detailSpinnerAufgabe;

	@FXML
	private Button detailCommit;

	@FXML
	private ScrollPane detailScrollPane;

	@FXML
	private GridPane paneRelations;

	@FXML
	private GridPane paneContacts;

	@FXML
	private TextField filterText;

	private ObservableList<Person> personData = FXCollections.observableArrayList();

	private ObservablePerson currentSelected = null;

	private PersonRepository personRepository;
	private ContactRepository contactRepository;
	private RelativeRepository relativeRepository;

	@FXML
	private void initialize() {
		System.out.println("initialize in " + getClass().getSimpleName() + " --> Table = " + tblPersonen);
	}

	@Autowired
	public void setContactRepository(ContactRepository contactRepository) {
		this.contactRepository = contactRepository;
	}

	@Autowired
	public void setPersonRepository(PersonRepository personRepository) {
		this.personRepository = personRepository;
	}

	@Autowired
	public void setRelativeRepository(RelativeRepository relativeRepository) {
		this.relativeRepository = relativeRepository;
	}

	@FXML
	private void storePerson() {
		if (currentSelected != null && currentSelected.hasChanges) {
			try {
				Person original = (Person) currentSelected.p.clone();
				try {
					currentSelected.changesToPerson();

					tblPersonen.refresh();
					personRepository.update(currentSelected.p);

					actions.add(new RepositoryUpdateAction<Person>(original, currentSelected.p, personRepository));

				} catch (IOException e) {
					new ExceptionDialog(e).show();
				}
			} catch (CloneNotSupportedException e1) {
				new ExceptionDialog(e1).show();
			}
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		background.execute(new Runnable() {

			@Override
			public void run() {
				try {
					personData.addAll(personRepository.all());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		if (tblPersonen != null) {

			columnPrename.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPrename()));
			columnSurname.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSurname()));
			columnAge.setCellValueFactory(cellData -> {
				String age = getAge(cellData.getValue());
				return new SimpleStringProperty(age);
			});

			if (personData.size() > 0) {
				tblPersonen.getSelectionModel().clearAndSelect(0);
			}

			tblPersonen.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Person>() {

				@Override
				public void changed(ObservableValue<? extends Person> observable, Person oldValue, Person newValue) {
					currentSelected = new ObservablePerson(newValue);
					updateDetails();
				}
			});

			FilteredList<Person> filteredData = new FilteredList<>(personData, p -> true);
			SortedList<Person> sortedData = new SortedList<>(filteredData);

			filterText.textProperty().addListener(new ChangeListener<String>() {

				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					filteredData.setPredicate(p -> {

						if (newValue == null || newValue.isEmpty())
							return true;

						String lowerCaseFilter = newValue.toLowerCase();

						if (p.getPrename().toLowerCase().contains(lowerCaseFilter))
							return true;

						if (p.getSurname().toLowerCase().contains(lowerCaseFilter))
							return true;

						return false;

					});
				}
			});

			sortedData.comparatorProperty().bind(tblPersonen.comparatorProperty());

			tblPersonen.setItems(sortedData);

		}
	}

	private void updateDetails() {
		if (currentSelected != null) {
			detailPersonPrename.textProperty().bindBidirectional(currentSelected.prenameProperty);
			detailPersonSurname.textProperty().bindBidirectional(currentSelected.surnameProperty);
			System.out.println("Editable: " + detailPersonSurname.isEditable());
			detailBirthday.valueProperty().bindBidirectional(currentSelected.birth);
			detailAge.setText(getAge(currentSelected.p));

			System.out.println("Submitting ContactFetchTask");
			background.execute(new ContactFetchTask(currentSelected.p));
			background.execute(new RelationFetchTask(currentSelected.p));

		} else {
			detailPersonPrename.setText("");
			detailPersonSurname.setText("");
			detailBirthday.setValue(null);
		}
	}

	@Value("PersonOverview.fxml")
	@Override
	public void setFxmlFilePath(String filePath) {
		this.fxmlFilePath = filePath;
	}

	private class ObservablePerson {

		private final Person p;

		private boolean hasChanges;

		public StringProperty prenameProperty;
		public StringProperty surnameProperty;
		public ObjectProperty<LocalDate> birth;

		public ObservablePerson(Person p) {
			super();
			this.p = p;

			hasChanges = false;

			prenameProperty = new SimpleStringProperty(p.getPrename());
			surnameProperty = new SimpleStringProperty(p.getSurname());

			if (p.getBirth() != null)
				birth = new SimpleObjectProperty<LocalDate>(
						p.getBirth().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
			else
				birth = new SimpleObjectProperty<LocalDate>();

			initListener();
		}

		private void initListener() {

			prenameProperty.addListener(new MyChangeListener<>());
			surnameProperty.addListener(new MyChangeListener<>());
			birth.addListener(new MyChangeListener<>());
		}

		public void changesToPerson() {
			p.setBirth(Date.from(birth.get().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
			p.setPrename(prenameProperty.get());
			p.setSurname(surnameProperty.get());
		}

		private class MyChangeListener<T> implements ChangeListener<T> {

			@Override
			public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
				if ((oldValue == null && newValue != null) || !oldValue.equals(newValue))
					hasChanges = true;
			}

		}
	}

	protected String getAge(Person p) {

		if (p == null || p.getBirth() == null) {
			return "";
		}

		StringBuilder bld = new StringBuilder();

		LocalDate birth = p.getBirth().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate now = LocalDate.now();
		Period between = Period.between(birth, now);
		bld.append(between.getYears()).append(" Jahre (").append(birth.getYear()).append(")");

		return bld.toString();
	}

	private class RelationFetchTask extends Task<List<Relative>> {

		private final Person person;
		List<Relative> result = Collections.emptyList();

		public RelationFetchTask(Person p) {
			this.person = p;
		}

		@Override
		protected List<Relative> call() throws Exception {
			result = relativeRepository.getByParentId(person.getId());
			return result;
		}

		@Override
		protected void succeeded() {
			super.succeeded();

			paneRelations.getChildren().clear();

			int rowIndex = 0;

			for (Relative r : result) {
				try {

					String relation;
					Person p;
					if (person.getId() == r.getPerson1()) {
						relation = r.getToPerson2Relation();
						p = personRepository.getById(r.getPerson2());
					} else {
						relation = r.getToPerson1Relation();
						p = personRepository.getById(r.getPerson1());
					}

					Label type = new Label(relation);
					Button btnValue = new Button(p.getPrename() + " " + p.getSurname());

					btnValue.setOnAction(new PersonLoadActionEventHandler(p));

					paneRelations.addRow(rowIndex, type, btnValue);
					rowIndex++;

				} catch (IOException e) {
					new ExceptionDialog(e).show();
				}
			}
		}

	}

	private class ContactFetchTask extends Task<List<Contact>> {

		private final Person person;
		List<Contact> result = Collections.emptyList();

		public ContactFetchTask(Person p) {
			this.person = p;
			System.out.println("ContactFetchTask initalized");
		}

		@Override
		protected List<Contact> call() throws Exception {
			System.out.println("Calling Repository...");
			try {
				result = contactRepository.getByParentId(person.getId());
			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
			System.out.println("Repo answer: " + result);
			return result;
		}

		@Override
		protected void succeeded() {
			super.succeeded();
			paneContacts.getChildren().clear();

			System.out.println("Updating Views...");
			int rowIndex = 0;

			for (Contact c : result) {
				Label type = new Label(c.getType());
				TextField value = new TextField(c.getValue());
				paneContacts.addRow(rowIndex, type, value);
				rowIndex++;
			}

		}
	}

	private class PersonLoadActionEventHandler implements EventHandler<ActionEvent> {

		private final Person person;

		public PersonLoadActionEventHandler(Person person) {
			super();
			this.person = person;
		}

		@Override
		public void handle(ActionEvent event) {
			currentSelected = new ObservablePerson(person);
			updateDetails();
		}

	}

	public void refreshView() {

		if (currentSelected != null) {
			try {
				int indexOf = personData.indexOf(currentSelected.p);
				currentSelected = new ObservablePerson(personRepository.getById(currentSelected.p.getId()));
				personData.remove(indexOf);
				personData.add(indexOf, currentSelected.p);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		updateDetails();
	}
}
