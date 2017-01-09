package de.kreth.clubhelperclient.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import de.kreth.clubhelperclient.action.ActionStack;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;

public abstract class FXMLController implements InitializingBean, Initializable {

	protected String fxmlFilePath;
	protected Node view;
	protected ActionStack actions;
	protected Logger log;

	public FXMLController() {
		log = Logger.getLogger(getClass());
	}

	public void setFxmlFilePath(String filePath) {
		this.fxmlFilePath = filePath;
	}

	// Wenn diese Methode bereitgestellt wird, kann auf das Interface
	// "Initializable" verzichtet werden.
	// public abstract void initialize();
	@Override
	public void afterPropertiesSet() throws Exception {
		loadFXML();
	}

	@Autowired
	public void setActions(ActionStack actionStack) {
		this.actions = actionStack;
	}

	protected final void loadFXML() throws IOException {
		URL resource = getClass().getResource(fxmlFilePath);

		try (InputStream fxmlStream = resource.openStream()) {

			if (fxmlStream == null) {
				throw new IOException("Resource " + fxmlFilePath + " not found!");
			}
			
			FXMLLoader loader = new FXMLLoader();
			loader.setController(this);

			try {
				this.view = loader.load(fxmlStream);
			} catch (Exception e) {
				log.error("Fehler beim Laden der FXML " + fxmlFilePath + "; URL: " + resource, e);
			}
		}
	}

	public Node getView() {
		return view;
	}

}
