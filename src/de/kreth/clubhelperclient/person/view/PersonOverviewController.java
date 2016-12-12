package de.kreth.clubhelperclient.person.view;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.controlsfx.dialog.ExceptionDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import de.kreth.clubhelperbackend.pojo.Contact;
import de.kreth.clubhelperbackend.pojo.Group;
import de.kreth.clubhelperbackend.pojo.Person;
import de.kreth.clubhelperbackend.pojo.PersonGroup;
import de.kreth.clubhelperbackend.pojo.PersonType;
import de.kreth.clubhelperbackend.pojo.Relative;
import de.kreth.clubhelperclient.action.RepositoryUpdateAction;
import de.kreth.clubhelperclient.core.FXMLController;
import de.kreth.clubhelperclient.person.model.ContactRepository;
import de.kreth.clubhelperclient.person.model.GroupRepository;
import de.kreth.clubhelperclient.person.model.PersonGroupRepository;
import de.kreth.clubhelperclient.person.model.PersonRepository;
import de.kreth.clubhelperclient.person.model.RelativeRepository;
import de.kreth.clubhelperclient.person.view.dialogs.PersonEditDialogController;
import de.kreth.clubhelperclient.reports.data.TrainingsPlanPerson;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

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
	private Button delPerson;

	@FXML
	private Label detailAge;

	@FXML
	private ListView<Group> groupListView;

	@FXML
	private ScrollPane detailScrollPane;

	@FXML
	private GridPane paneRelations;

	@FXML
	private GridPane paneContacts;

	@FXML
	private TextField filterText;

	private ObservableList<Person> personData = FXCollections.observableArrayList();
	private ObservableMap<Long, Group> allGroups = FXCollections.observableHashMap();
	private ObservableList<Group> personGroups = FXCollections.observableArrayList();

	private ObservablePerson currentSelected = null;
	private String currentTelephone = "";

	private PersonRepository personRepository;
	private ContactRepository contactRepository;
	private RelativeRepository relativeRepository;
	private GroupRepository groupRepository;
	private PersonGroupRepository personGroupRepository;

	public boolean relativesLoading;

	@FXML
	private void initialize() {
		// does nothing.
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

	@Autowired
	public void setGroupRepository(GroupRepository groupRepository) {
		this.groupRepository = groupRepository;
	}

	@Autowired
	public void setPersonGroupRepository(PersonGroupRepository personGroupRepository) {
		this.personGroupRepository = personGroupRepository;
	}

	@FXML
	private void showGroupContextMenu() {
		Alert msg = new Alert(AlertType.INFORMATION);
		msg.setContentText("Zeige jetzt auch das Menü?");
		msg.show();
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

	@FXML
	protected void addPerson(ActionEvent ev) {
		log.debug("Starting Add Person");

		PersonCreatedTask pTask = new PersonCreatedTask();

		URL resource = PersonEditDialogController.class.getResource("PersonEditDialog.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader(resource);

		Stage stage = new Stage();

		stage.initModality(Modality.WINDOW_MODAL);
		stage.initStyle(StageStyle.DECORATED);

		stage.initOwner(tblPersonen.getScene().getWindow());
		stage.setTitle("Neue Person");

		try {
			AnchorPane pane = fxmlLoader.load();

			Scene scene = new Scene(pane);
			stage.setScene(scene);

			PersonEditDialogController cont = fxmlLoader.getController();
			cont.setGroups(allGroups);
			cont.setHandler(pTask);
			cont.setStage(stage);

			stage.show();
		} catch (Exception e) {
			log.error("Cannot open Dialog", e);
		}
	}

	@FXML
	protected void delPerson(ActionEvent ev) {
		log.debug("deleting " + currentSelected);

	}

	protected void removeGroupFromUser(Group selectedItem2) {
		if (selectedItem2 != null) {
			personGroups.remove(selectedItem2);
			if (currentSelected != null && currentSelected.p != null) {
				log.info("Removed " + selectedItem2.getName() + " from " + currentSelected.p.toString());
			} else {
				log.warn("No Person selected currently!?");
			}
		} else {
			log.warn("Group to remove was " + selectedItem2);
		}
	}

	protected void addGroupToUser() {

		List<Group> available = new ArrayList<>();
		log.debug("AllGroups size: " + allGroups.size());
		log.trace("AllGroups: " + allGroups);

		for (Group g : allGroups.values()) {
			if (!personGroups.contains(g)) {
				available.add(new ToStringGroup(g));
			}
			available.remove(g);
		}

		log.info("Available Groups to choose from: " + available);

		ChoiceDialog<Group> dlg = new ChoiceDialog<>(available.get(0), available);
		dlg.setTitle("Gruppe hinzufügen");
		dlg.setHeaderText("Welche Gruppe soll " + currentSelected.p.toString() + " hinzugefügt werden?");
		Optional<Group> result = dlg.showAndWait();
		if (result.isPresent()) {
			Group group = result.get();
			personGroups.add(group);
			try {
				personGroupRepository
						.insert(new PersonGroup(-1L, currentSelected.p.getId(), group.getId(), null, null));
			} catch (IOException e) {
				personGroups.remove(group);
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Fehler bei Gruppe");
				alert.setContentText("Die hinzugefügte Gruppe konnte nicht gespeichert werden.\n" + e.getMessage());
				alert.show();
			}
		}
	}

	@FXML
	private void printTrainingsplan() {
		if (currentSelected != null) {
			Person p = currentSelected.p;

			TrainingsPlanPerson pers = new TrainingsPlanPerson(p, p.getCreated(), currentTelephone);

			List<TrainingsPlanPerson> srcList = new ArrayList<>();
			srcList.add(pers);

			try {
				String rs = "/Trainingsplan2.jasper";

				JRBeanCollectionDataSource source = new JRBeanCollectionDataSource(srcList);
				InputStream res = getClass().getResourceAsStream(rs);
				JasperPrint print = JasperFillManager.fillReport(res, new HashMap<>(), source);

				JasperViewer.viewReport(print, false);
			} catch (JRException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		refresh();

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
					if (newValue != null) {
						currentSelected = new ObservablePerson(newValue);
					} else {
						currentSelected = null;
					}
					currentTelephone = "";
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

			delPerson.setDisable(true);

			groupListView.setItems(personGroups);
			groupListView.setCellFactory(new Callback<ListView<Group>, ListCell<Group>>() {

				@Override
				public ListCell<Group> call(ListView<Group> param) {
					ListCell<Group> cell = new ListCell<Group>() {
						@Override
						protected void updateItem(Group item, boolean empty) {

							super.updateItem(item, empty);

							if (item != null) {
								String name = translate(item.getName());
								setText(name);
							}
						}

					};

					return cell;
				}
			});

			groupListView.setContextMenu(new GroupListContextMenu());

			groupListView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					if (event.getButton() == MouseButton.SECONDARY) {
						groupListView.getContextMenu().show(groupListView, event.getScreenX(), event.getScreenY());
					}

				}
			});
		}

	}

	private String translate(String name) {
		if (name.equals(PersonType.ACTIVE.name())) {
			name = "Aktiver";
		} else if (name.equals(PersonType.RELATIVE.name())) {
			name = "Verwandter";
		} else if (name.equals(PersonType.STAFF.name())) {
			name = "Funktionär";
		}
		return name;
	}

	public void refresh() {

		background.execute(new Runnable() {

			@Override
			public void run() {
				try {
					personData.clear();
					personData.addAll(personRepository.all());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		allGroups.clear();
		background.execute(new GroupFetchTask());
	}

	public void updateDetails() {

		personGroups.clear();

		if (currentSelected != null) {
			detailPersonPrename.textProperty().bindBidirectional(currentSelected.prenameProperty);
			detailPersonSurname.textProperty().bindBidirectional(currentSelected.surnameProperty);
			log.info("Editable: " + detailPersonSurname.isEditable());

			detailBirthday.valueProperty().bindBidirectional(currentSelected.birth);
			detailAge.setText(getAge(currentSelected.p));

			log.trace("Submitting ContactFetchTask");
			background.execute(new ContactFetchTask(currentSelected.p));
			background.execute(new RelationFetchTask(currentSelected.p));
			background.execute(new PersonGroupFetchTask(currentSelected.p));

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
			LocalDate localDate = birth.get();

			if (localDate != null) {
				p.setBirth(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
			} else {
				p.setBirth(null);
			}

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

		@Override
		public String toString() {
			return p != null ? p.toString() : "";
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

	private class PersonGroupFetchTask extends Task<List<PersonGroup>> {

		private final Person person;
		private List<PersonGroup> result = Collections.emptyList();

		public PersonGroupFetchTask(Person person) {
			this.person = person;
		}

		@Override
		protected List<PersonGroup> call() throws Exception {

			result = personGroupRepository.getByParentId(person.getId());
			return result;
		}

		@Override
		protected void succeeded() {

			super.succeeded();
			if (result.isEmpty()) {
				String type = person.getType();
				Group group = null;

				for (Group g : allGroups.values()) {
					if (g.getName().equals(type)) {
						group = g;
						break;
					}
				}

				if (group == null) {
					group = new Group(-1L, type, null, null);
					final Group g1 = group;
					log.warn("Group for type " + type + " does not exist! creating and updating Person.");
					background.execute(new Task<Void>() {

						Group inserted;

						@Override
						protected Void call() throws Exception {
							try {
								Group temp = groupRepository.insert(g1);
								PersonGroup pg = new PersonGroup(-1L, PersonGroupFetchTask.this.person.getId(),
										temp.getId(), null, null);
								personGroupRepository.insert(pg);
								inserted = temp;
							} catch (IOException e) {
								e.printStackTrace();
							}
							return null;
						}

						@Override
						protected void succeeded() {
							super.succeeded();
							personGroups.add(inserted);
							groupListView.refresh();
						}
					});
				} else {
					log.warn("Persontype exists as group, updating Person with group.");
					personGroups.add(group);
					final PersonGroup pg = new PersonGroup(-1L, PersonGroupFetchTask.this.person.getId(), group.getId(),
							null, null);

					background.execute(new Runnable() {

						@Override
						public void run() {
							try {
								personGroupRepository.insert(pg);
							} catch (IOException e) {
								log.warn("could not insert PerosnGroup for " + person + " with groupId="
										+ pg.getGroupId(), e);
							}
						}
					});

				}
			} else {
				for (PersonGroup g : result) {
					if (allGroups.containsKey(g.getGroupId())) {
						personGroups.add(allGroups.get(g.getGroupId()));
					}
				}
			}

		}
	}

	private class RelationFetchTask extends Task<List<Relative>> {

		private final Person person;
		List<Relative> result = Collections.emptyList();

		public RelationFetchTask(Person p) {
			this.person = p;
		}

		@Override
		protected List<Relative> call() throws Exception {
			relativesLoading = true;
			try {
				result = relativeRepository.getByParentId(person.getId());
			} catch (Exception e) {
				relativesLoading = false;
				throw e;
			}
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

					if (p != null) {

						Label type = new Label(relation);
						Button btnValue = new Button(p.getPrename() + " " + p.getSurname());

						btnValue.setOnAction(new PersonLoadActionEventHandler(p));

						paneRelations.addRow(rowIndex, type, btnValue);
						rowIndex++;

					}

				} catch (IOException e) {
					new ExceptionDialog(e).show();
				}
			}
		}

	}

	private class GroupFetchTask extends Task<List<Group>> {

		@Override
		protected List<Group> call() throws Exception {
			log.debug("Calling Group Repository...");
			List<Group> groups = groupRepository.all();
			for (Group g : groups) {
				allGroups.put(g.getId(), g);
			}
			return null;
		}

	}

	private class ContactFetchTask extends Task<List<Contact>> {

		private final Person person;
		List<Contact> result = Collections.emptyList();

		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

		public ContactFetchTask(Person p) {
			this.person = p;
			log.trace("ContactFetchTask initalized");
		}

		@Override
		protected List<Contact> call() throws Exception {
			log.debug("Calling Contact Repository...");

			try {
				result = contactRepository.getByParentId(person.getId());
				if (result == null || result.isEmpty()) {
					result = new ArrayList<>();
					while (relativesLoading) {
						try {
							Thread.sleep(100);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					while (result.isEmpty()) {

					}
				}
			} catch (Exception e) {
				log.info("Error getting contacts.", e);
			}
			log.info("Repo answer: " + result);
			return result;
		}

		@Override
		protected void succeeded() {
			super.succeeded();
			paneContacts.getChildren().clear();

			log.debug("Updating Views...");

			int rowIndex = 0;

			String lastType = null;

			for (Contact c : result) {

				String type2 = c.getType();

				if (lastType == null) {
					try {
						PhoneNumber parsed = phoneUtil.parse(c.getValue(), "DE");
						currentTelephone = phoneUtil.format(parsed, PhoneNumberFormat.NATIONAL);
						lastType = type2;
					} catch (NumberParseException e) {
						e.printStackTrace();
					}

				} else {
					if (!lastType.toLowerCase().contains("mobile") && type2.toLowerCase().contains("mobile")) {
						try {
							PhoneNumber parsed = phoneUtil.parse(c.getValue(), "DE");
							currentTelephone = phoneUtil.format(parsed, PhoneNumberFormat.NATIONAL);
							lastType = type2;
						} catch (NumberParseException e) {
							e.printStackTrace();
						}
					}
				}
				Label type = new Label(type2);
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

			delPerson.setDisable(false);
			try {
				int indexOf = personData.indexOf(currentSelected.p);
				currentSelected = new ObservablePerson(personRepository.getById(currentSelected.p.getId()));
				personData.remove(indexOf);
				personData.add(indexOf, currentSelected.p);
			} catch (IOException e) {
				delPerson.setDisable(true);
				e.printStackTrace();
			}

		} else {
			delPerson.setDisable(true);
		}

		updateDetails();
	}

	private class PersonCreatedTask extends Task<Person> implements PersonEditDialogController.PersonCreated {

		private Person p;

		@Override
		public void setPerson(Person p) {
			this.p = p;
			background.execute(this);
		}

		@Override
		protected Person call() throws Exception {
			p = personRepository.insert(p);
			return p;
		}

		@Override
		protected void succeeded() {
			super.succeeded();
			currentSelected = new ObservablePerson(p);
			updateDetails();
		}
	}

	private class GroupListContextMenu extends ContextMenu {

		private Group selectedItem;

		public GroupListContextMenu() {
			setOnShowing(new EventHandler<WindowEvent>() {

				@Override
				public void handle(WindowEvent event) {
					selectedItem = groupListView.getSelectionModel().getSelectedItem();
				}
			});

			MenuItem item1 = new MenuItem("Hinzufügen");
			item1.setOnAction(new EventHandler<ActionEvent>() {
				public void handle(ActionEvent e) {
					addGroupToUser();
				}
			});

			MenuItem item2 = new MenuItem("Entfernen");
			item2.setOnAction(new EventHandler<ActionEvent>() {
				public void handle(ActionEvent e) {
					if (selectedItem != null) {
						removeGroupFromUser(selectedItem);
					} else {
						log.warn("No Group selected for delete.");
					}
				}
			});
			getItems().addAll(item1, item2);
		}

	}

	private class ToStringGroup extends Group {

		private static final long serialVersionUID = -1305476082760844995L;

		public ToStringGroup(Group parent) {
			super(parent.getId(), parent.getName(), parent.getChanged(), parent.getCreated());
		}

		@Override
		public String toString() {
			return translate(getName());
		}
	}
}
