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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.controlsfx.dialog.ExceptionDialog;
import org.controlsfx.dialog.ProgressDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.kreth.clubhelperbackend.pojo.Contact;
import de.kreth.clubhelperbackend.pojo.Group;
import de.kreth.clubhelperbackend.pojo.Person;
import de.kreth.clubhelperbackend.pojo.PersonGroup;
import de.kreth.clubhelperbackend.pojo.PersonType;
import de.kreth.clubhelperbackend.pojo.Relative;
import de.kreth.clubhelperclient.action.RepositoryUpdateAction;
import de.kreth.clubhelperclient.core.FXMLController;
import de.kreth.clubhelperclient.group.GroupEditorController;
import de.kreth.clubhelperclient.person.model.ContactRepository;
import de.kreth.clubhelperclient.person.model.GroupRepository;
import de.kreth.clubhelperclient.person.model.PersonGroupRepository;
import de.kreth.clubhelperclient.person.model.PersonRepository;
import de.kreth.clubhelperclient.person.model.RelativeRepository;
import de.kreth.clubhelperclient.person.model.SelectablePerson;
import de.kreth.clubhelperclient.person.view.ContactRowController.ContactDeleted;
import de.kreth.clubhelperclient.person.view.dialogs.PersonEditDialogController;
import de.kreth.clubhelperclient.reports.data.ListPerson;
import de.kreth.clubhelperclient.reports.data.PhoneListPerson;
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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
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
	private Button groupRemoveBtn;

	@FXML
	private Label detailAge;

	@FXML
	private ListView<PersonGroup> groupListView;

	@FXML
	private GridPane paneRelations;

	@FXML
	private VBox paneContacts;

	@FXML
	private TextField filterText;

	@FXML
	private Button printLists;

	private ObservableList<Person> personData = FXCollections.observableArrayList();
	private ObservableMap<Long, Group> allGroups = FXCollections.observableHashMap();
	private ObservableMap<Long, ObservableList<PersonGroup>> personGroups = FXCollections.observableHashMap();

	private ObservablePerson currentSelected = null;
	private ObservableList<PersonGroup> currentPersonsGroups = FXCollections.observableArrayList();

	private String currentTelephone = "";

	private PersonRepository personRepository;
	private ContactRepository contactRepository;
	private RelativeRepository relativeRepository;
	private GroupRepository groupRepository;
	private PersonGroupRepository personGroupRepository;

	public boolean relativesLoading;

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

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		refreshPersonList();
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
		Alert dlg = new Alert(AlertType.CONFIRMATION);
		dlg.setTitle("Löschen bestätigen");
		dlg.setHeaderText(currentSelected.p.toString());
		dlg.setContentText("Soll diese Person wirklich gelöscht werden?");
		Optional<ButtonType> res = dlg.showAndWait();
		if (res.isPresent()) {
			ButtonType buttonType = res.get();
			final Person toDelete = currentSelected.p;
			if (ButtonType.YES.equals(buttonType) || ButtonType.OK.equals(buttonType)) {
				background.execute(new Task<Void>() {

					@Override
					protected Void call() throws Exception {
						personRepository.delete(toDelete);
						return null;
					}

					@Override
					protected void failed() {
						super.failed();
						if (log.isDebugEnabled()) {
							ExceptionDialog dlg = new ExceptionDialog(getException());
							dlg.setHeaderText("nicht gelöscht: " + toDelete);
						}
						log.warn(toDelete + " not deleted!", getException());
					}

					@Override
					protected void succeeded() {
						super.succeeded();
						log.debug("Successfully deleted " + toDelete + " on server. Removing...");
						personData.remove(toDelete);
					}
				});

			}
		}
	}

	@FXML
	private void removeGroupFromPerson() {
		final PersonGroup selectedItem = groupListView.getSelectionModel().getSelectedItem();
		if (selectedItem != null) {
			log.info("Removing " + selectedItem + " from " + currentSelected);
			personGroups.get(selectedItem.getPersonId()).remove(selectedItem);
			currentPersonsGroups.remove(selectedItem);
			
			background.execute(new Runnable() {
				
				@Override
				public void run() {

					try {
						personGroupRepository.delete(selectedItem);
						log.debug("Successfully removed " + selectedItem + " from " + currentSelected);
					} catch (IOException e) {
						log.error("Unable to remove " + selectedItem + " from " + currentSelected);
					}
				}
			});
		}
	}

	@FXML
	private void showGroupEditor() {
		GroupEditorController contr = new GroupEditorController(groupRepository);

		FXMLLoader ldr = new FXMLLoader();
		ldr.setLocation(contr.getClass().getResource("GroupEditor.fxml"));
		ldr.setController(contr);
		try {
			Parent view = ldr.load();
			contr.init(allGroups);
			Stage stage = new Stage();

			stage.initModality(Modality.WINDOW_MODAL);
			stage.initStyle(StageStyle.DECORATED);

			stage.initOwner(tblPersonen.getScene().getWindow());
			stage.setTitle("Neue Person");

			Scene sc = new Scene(view);
			stage.setScene(sc);
			stage.show();

		} catch (IOException e) {
			log.error("Cannot show GroupEditor", e);
		}
	}

	protected void removeGroupFromUser(PersonGroup selectedItem) {
		if (selectedItem != null) {
			personGroups.remove(selectedItem);
			if (currentSelected != null && currentSelected.p != null) {
				log.info("Removed " + allGroups.get(selectedItem.getId()).getName() + " from "
						+ currentSelected.p.toString());
			} else {
				log.warn("No Person selected currently!?");
			}
		} else {
			log.warn("Group to remove was " + selectedItem);
		}
	}

	@FXML
	protected void addGroupToPerson() {

		List<Group> available = new ArrayList<>();
		log.debug("AllGroups size: " + allGroups.size());
		log.trace("AllGroups: " + allGroups);

		final ObservableList<PersonGroup> pg = FXCollections.observableArrayList();
		if (personGroups.containsKey(currentSelected.p.getId())) {
			pg.addAll(personGroups.get(currentSelected.p.getId()));
		} else {
			personGroups.put(currentSelected.p.getId(), pg);
		}

		for (Group g : allGroups.values()) {
			boolean found = false;
			for (PersonGroup persGr : pg) {
				if (persGr.getId() == g.getId()) {
					found = true;
					break;
				}
			}
			if (found == false) {
				available.add(new ToStringGroup(g));
			}
		}

		log.info("Available Groups to choose from: " + available);

		ChoiceDialog<Group> dlg = new ChoiceDialog<>(available.get(0), available);
		dlg.setTitle("Gruppe hinzufügen");
		dlg.setHeaderText("Welche Gruppe soll " + currentSelected.p.toString() + " hinzugefügt werden?");
		Optional<Group> result = dlg.showAndWait();
		if (result.isPresent()) {
			Group group = result.get();
			PersonGroup inserted = null;
			try {
				inserted = personGroupRepository
						.insert(new PersonGroup(-1L, currentSelected.p.getId(), group.getId(), null, null));

				pg.add(inserted);
			} catch (IOException e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Fehler bei Gruppe");
				alert.setContentText("Die hinzugefügte Gruppe konnte nicht gespeichert werden.\n" + e.getMessage());
				alert.show();
			}
		}
	}

	@FXML
	public void showPrintListOptions() {
		final ListView<SelectablePerson> list = new ListView<>();
		list.getItems().addAll(personData.stream().sorted(ListPerson.personComparator())
				.map(p -> new SelectablePerson(p)).collect(Collectors.toList()));

		list.setCellFactory(CheckBoxListCell.forListView(SelectablePerson::selectedProperty,
				new StringConverter<SelectablePerson>() {

					@Override
					public String toString(SelectablePerson object) {
						return object.nameProperty().get();
					}

					@Override
					public SelectablePerson fromString(String string) {
						Optional<SelectablePerson> first = list.getItems().stream()
								.filter(p -> p.nameProperty().get().equals(string)).findFirst();
						if (first.isPresent()) {
							return first.get();
						}
						return null;
					}
				}));
		Dialog<List<Person>> dlg = new Dialog<>();
		dlg.initModality(Modality.WINDOW_MODAL);
		dlg.setResizable(true);
		DialogPane dialogPane = dlg.getDialogPane();
		dialogPane.setContent(list);
		ObservableList<ButtonType> buttonTypes = dialogPane.getButtonTypes();
		buttonTypes.add(ButtonType.OK);
		buttonTypes.add(ButtonType.CANCEL);
		dlg.setResultConverter(button -> {
			List<Person> result = new ArrayList<>();
			if (button == ButtonType.OK) {
				result.addAll(list.getItems().stream().filter(selectable -> selectable.selectedProperty().get())
						.collect(Collectors.toList()));
			}
			return result;
		});
		Optional<List<Person>> res = dlg.showAndWait();

		List<Person> list2 = res.get();

		if (list2.isEmpty()) {
			Alert dlg2 = new Alert(AlertType.WARNING);
			dlg2.setContentText("Niemand ausgewählt. Abbruch.");
			dlg2.show();
		} else
			createPhoneList(list2);
	}

	private void createPhoneList(final List<Person> list) {

		final List<PhoneListPerson> srcList = new ArrayList<>();

		Task<Void> personTask = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				Function<Person, PhoneListPerson> mapper = (p) -> {
					try {
						log.trace("Processing " + p);
						return PhoneListPerson.createAndFill(p, personData, relativeRepository, contactRepository);
					} catch (InterruptedException | ExecutionException e) {
						log.error("Error creating PhoneList Person", e);
					}
					return null;
				};
				final int max = list.size();
				list.stream().sorted(PhoneListPerson.personComparator()).map(mapper).filter(p -> p != null)
						.forEach(p -> {
							srcList.add(p);
							updateProgress(srcList.size(), max);
						});

				return null;
			}

			@Override
			protected void succeeded() {
				super.succeeded();

				String rs = "/PhoneList.jasper";

				JRBeanCollectionDataSource source = new JRBeanCollectionDataSource(srcList);
				InputStream res = getClass().getResourceAsStream(rs);
				JasperPrint print;
				try {
					print = JasperFillManager.fillReport(res, new HashMap<>(), source);
					JasperViewer.viewReport(print, false);
				} catch (JRException e) {
					log.error("Unable to create PhoneList", e);
				}
			}
		};

		ProgressDialog progDlg = new ProgressDialog(personTask);
		progDlg.initModality(Modality.WINDOW_MODAL);
		progDlg.initOwner(view.getScene().getWindow());
		progDlg.setTitle("Lade Personendaten für Telefonliste");
		progDlg.setHeaderText("Bitte warten...");

		try {
			background.execute(personTask);
		} catch (Exception e1) {
			log.error("Unable to create PhoneList", e1);
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

				InputStream res = getClass().getResourceAsStream(rs);
				if (res == null) {
					log.error("Resource " + rs + " not found. Unable to create Report");
					return;
				}
				JRBeanCollectionDataSource source = new JRBeanCollectionDataSource(srcList);
				JasperPrint print = JasperFillManager.fillReport(res, new HashMap<>(), source);

				JasperViewer.viewReport(print, false);
			} catch (JRException e) {
				log.error("Unable to create Report", e);
			}
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

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
						delPerson.setDisable(false);
					} else {
						currentSelected = null;
						delPerson.setDisable(true);
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

			groupListView.setItems(currentPersonsGroups);
			groupListView.setCellFactory(new Callback<ListView<PersonGroup>, ListCell<PersonGroup>>() {

				@Override
				public ListCell<PersonGroup> call(ListView<PersonGroup> param) {
					ListCell<PersonGroup> cell = new ListCell<PersonGroup>() {
						@Override
						protected void updateItem(PersonGroup item, boolean empty) {

							super.updateItem(item, empty);

							if (item != null) {
								String name = translate(allGroups.get(item.getGroupId()).getName());
								setText(name);
							} else {
								setText(null);
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
			groupListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PersonGroup>() {

				@Override
				public void changed(ObservableValue<? extends PersonGroup> observable, PersonGroup oldValue,
						PersonGroup newValue) {
					if(groupRemoveBtn == null) {
						log.warn("GroupRemove not found!");
						return;
					}
					if (newValue != null) {
						groupRemoveBtn.setDisable(false);
					} else {
						groupRemoveBtn.setDisable(true);
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

	@FXML
	public void refreshPersonList() {

		Task<Void> personTask = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				personData.clear();
				personData.addAll(personRepository.all());
				return null;
			}

			@Override
			protected void succeeded() {
				for (Person p : personData) {
					background.execute(new PersonGroupFetchTask(p));
				}
				super.succeeded();
			}
		};

		background.execute(new GroupFetchTask());

		ProgressDialog progDlg = new ProgressDialog(personTask);
		progDlg.initModality(Modality.WINDOW_MODAL);
		if (view != null) {
			Scene scene = view.getScene();
			if (scene != null) {
				progDlg.initOwner(scene.getWindow());
			}
		}
		progDlg.setTitle("Lade Personendaten");
		progDlg.setContentText("Bitte warten...");

		background.execute(personTask);

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
			currentPersonsGroups.clear();
			if (personGroups.containsKey(currentSelected.getId())) {
				currentPersonsGroups.addAll(personGroups.get(currentSelected.getId()));
			} else {
				background.execute(new PersonGroupFetchTask(currentSelected.p));
			}

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

		public Long getId() {
			return p.getId();
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
			if (personGroups.containsKey(person.getId()) == false) {
				personGroups.put(person.getId(), FXCollections.observableArrayList());
			}
			final ObservableList<PersonGroup> persGrList = personGroups.get(person.getId());
			persGrList.clear();

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

						PersonGroup pg;

						@Override
						protected Void call() throws Exception {
							try {
								Group temp = groupRepository.insert(g1);
								allGroups.put(temp.getId(), temp);
								pg = new PersonGroup(-1L, PersonGroupFetchTask.this.person.getId(), temp.getId(), null,
										null);
								pg = personGroupRepository.insert(pg);
								persGrList.add(pg);
							} catch (IOException e) {
								e.printStackTrace();
							}
							return null;
						}

						@Override
						protected void succeeded() {
							super.succeeded();
							groupListView.refresh();
						}
					});
				} else {
					log.warn("Persontype exists as group, updating Person with group.");

					final PersonGroup pg = new PersonGroup(-1L, PersonGroupFetchTask.this.person.getId(), group.getId(),
							null, null);

					background.execute(new Runnable() {

						@Override
						public void run() {
							try {
								persGrList.add(personGroupRepository.insert(pg));
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
						persGrList.add(g);
					}
				}
				if (currentSelected != null && currentSelected.getId().equals(person.getId())) {
					currentPersonsGroups.clear();
					currentPersonsGroups.addAll(persGrList);
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

			allGroups.clear();
			for (Group g : groups) {
				allGroups.put(g.getId(), g);
			}
			return null;
		}

	}

	private class ContactFetchTask extends Task<List<Contact>> {

		private final Person person;
		List<Contact> result = Collections.emptyList();

		public ContactFetchTask(Person p) {
			this.person = p;
			log.trace("ContactFetchTask initalized");
		}

		@Override
		protected List<Contact> call() throws Exception {
			log.debug("Fetching contacts for " + person);

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

			for (Contact c : result) {

				final FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource("ContactRow.fxml"));
				ContactRowController con = new ContactRowController(c, background, contactRepository);

				loader.setController(con);

				try {
					Node contactRow = loader.load();
					con.setDeleteAction(new RemoveRowTask());
					con.init();
					paneContacts.getChildren().add(contactRow);
				} catch (IOException e1) {
					log.error("Cound not add Contact: " + c, e1);
				}

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

		private PersonGroup selectedItem;

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
					addGroupToPerson();
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

	private class RemoveRowTask extends Task<Void> implements ContactDeleted {

		private Parent rowItem;
		private Contact c;

		@Override
		public void deleted(Contact c, Parent rowItem) {
			Integer index = GridPane.getRowIndex(rowItem);
			log.debug("removing Contact " + c + " in Row " + index);
			this.rowItem = rowItem;
			this.c = c;
			this.run();
		}

		@Override
		protected Void call() throws Exception {
			return null;
		}

		@Override
		protected void succeeded() {
			super.succeeded();
			log.debug("removing Contact " + c);
			paneContacts.getChildren().remove(rowItem);
		}
	}
}
