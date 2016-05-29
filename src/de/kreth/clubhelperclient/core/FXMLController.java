package de.kreth.clubhelperclient.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.springframework.beans.factory.InitializingBean;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;

public abstract class FXMLController implements InitializingBean, Initializable {

	protected String fxmlFilePath;
	protected Node view;

	public abstract void setFxmlFilePath(String filePath);

	// Wenn diese Methode bereitgestellt wird, kann auf das Interface
	// "Initializable" verzichtet werden.
	// public abstract void initialize();
	@Override
	public void afterPropertiesSet() throws Exception {
		loadFXML();
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
				this.view = (loader.load(fxmlStream));
			} catch (Exception e) {
				System.err.println("Fehler beim Laden der FXML " + fxmlFilePath);
				e.printStackTrace(System.err);
			}
		}
	}

	public Node getView() {
		return view;
	}

}
