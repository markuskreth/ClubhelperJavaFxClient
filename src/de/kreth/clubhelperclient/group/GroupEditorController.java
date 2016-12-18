package de.kreth.clubhelperclient.group;

import java.io.IOException;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import de.kreth.clubhelperbackend.pojo.Group;
import de.kreth.clubhelperclient.person.model.GroupRepository;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

public class GroupEditorController {

	@FXML
	private ListView<Group> groupList;
	
	@FXML 
	private Button storeChanges;
	
	@FXML
	private Button revertChanges;
	
	@FXML 
	private TextField groupEditor;
	
	private GroupRepository groupRepository;

	private ObservableMap<Long, Group> allGroups;
	private ObservableList<Group> groups;

	protected ObservableValue<? extends Group> currentGroup;

	private Logger log;
	
	public GroupEditorController(GroupRepository groupRepository) {
		this.groupRepository = groupRepository;
	}

	@FXML
	public void addGroup(){
		currentGroup = null;
		
		groupEditor.setText("");
		groupEditor.requestFocus();
	}
	
	@FXML
	public void storeChanges() {

		Alert dlg = new Alert(AlertType.CONFIRMATION);

		dlg.getButtonTypes().clear();
		
		Group current;
		if(currentGroup == null) {
			current = new Group(-1L, groupEditor.getText(), null, null);
			dlg.setTitle("Neue Gruppe");
			dlg.setHeaderText(groupEditor.getText() + " anlegen?");
		} else {
			dlg.setTitle("Gruppe speichern");
			dlg.setHeaderText(groupEditor.getText() + " ändern?");
			current = currentGroup.getValue();
			current.setName(groupEditor.getText());			
		}

		dlg.getButtonTypes().add(ButtonType.YES);
		dlg.getButtonTypes().add(ButtonType.NO);
		Optional<ButtonType> result = dlg.showAndWait();
		if(result.isPresent() && ButtonType.YES.equals(result.get())) {
			
				try {
					if(currentGroup == null) {
						current = groupRepository.insert(current);
						allGroups.put(current.getId(), current);
						groups.add(current);
					} else {
						groupRepository.update(current);
					}
				} catch (IOException e) {
					log.error("Can't store Group " + current, e);
				}
			
		}
	}
	
	@FXML
	public void revertChanges() {
		
	}

	@FXML
	public void deleteGroup() {
		
	}
	
	public void init(ObservableMap<Long, Group> allGroups) {
		Assert.notNull(groupEditor);
		this.log = Logger.getLogger(getClass());
				
		this.allGroups = allGroups;
		this.groups = FXCollections.observableArrayList(allGroups.values());
		groupList.setItems(this.groups);
		groupList.setCellFactory(new Callback<ListView<Group>, ListCell<Group>>() {
			
			@Override
			public ListCell<Group> call(ListView<Group> param) {
				ListCell<Group> cell = new ListCell<Group>() {
					@Override
					protected void updateItem(Group item, boolean empty) {
						super.updateItem(item, empty);
						if(item != null) {
							setText(item.getName());
						} else {
							setText("");
						}
					}
				};
				
				return cell;
			}
		});
		groupList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Group>() {

			@Override
			public void changed(ObservableValue<? extends Group> observable, Group oldValue, Group newValue) {
				if(newValue != null) {
					groupEditor.setText(newValue.getName());
				}
				currentGroup = observable;
			}
		});
	}
}
