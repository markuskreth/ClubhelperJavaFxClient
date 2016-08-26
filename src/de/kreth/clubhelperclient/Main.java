package de.kreth.clubhelperclient;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.controlsfx.dialog.ExceptionDialog;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import de.kreth.clubhelperbackend.pojo.Data;
import de.kreth.clubhelperclient.action.Action;
import de.kreth.clubhelperclient.action.ActionStack;
import de.kreth.clubhelperclient.core.RemoteHolder;
import de.kreth.clubhelperclient.person.view.PersonOverviewController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@Configuration
@ComponentScan("de.kreth.clubhelperclient")
@PropertySource("classpath:application.properties")
public class Main extends Application {

	private static final String REMOTE_KEY = "remote.server.list";

	private Preferences prefs = Preferences.userNodeForPackage(Application.class);

	private ApplicationContext appContext;
	private Stage primaryStage;
	private BorderPane rootLayout;

	@FXML
	private MenuBar menuRoot;

	@FXML
	private Menu menuServerUrl;

	@FXML
	private RadioMenuItem menuItemPrototype;

	private ActionStack actionStack;

	private PersonOverviewController controller;

	@Override
	public void start(Stage primaryStage) {
		try {

			this.primaryStage = primaryStage;

			initSpringFramework();

			initRootLayout();

			initServerMenuItems();

			showPersonOverview();

		} catch (Exception e) {
			ExceptionDialog dlg = new ExceptionDialog(e);
			dlg.show();
		}
	}


	@FXML
	public void onRevertAction(final ActionEvent event) {

		if (!actionStack.isEmpty()) {
			Action<?> action = actionStack.pop();
			Data original = action.getOriginal();
			action.revert();
			System.out.println(original.toString() + " wieder hergestellt.");
			controller.refreshView();
		}

	}

	private void initServerMenuItems() {

		final RemoteHolder remoteHolder = appContext.getBean(RemoteHolder.class);

		EventHandler<ActionEvent> menuHandler = new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				String text = ((MenuItem) event.getSource()).getText();
				remoteHolder.setRemoteUrl(text);
				showPersonOverview();
				prefs.put(REMOTE_KEY, text);
				try {
					prefs.flush();
				} catch (BackingStoreException e) {
					e.printStackTrace();
				}
			}
		};

		if (menuItemPrototype != null) {

			menuItemPrototype.setText("http://localhost:8090/ClubHelperBackend/");

			if (menuItemPrototype.getText().equals(remoteHolder.getRemoteUrl()))
				menuItemPrototype.setSelected(true);

			menuItemPrototype.setOnAction(menuHandler);

			RadioMenuItem item2 = new RadioMenuItem("http://localhost:8080/ClubHelperBackend/");

			if (item2.getText().equals(remoteHolder.getRemoteUrl()))
				item2.setSelected(true);
			item2.setOnAction(menuHandler);
			menuServerUrl.getItems().add(item2);

			RadioMenuItem item3 = new RadioMenuItem("http://markuskreth.kreinacke.de:8080/ClubHelperBackend");

			if (item3.getText().equals(remoteHolder.getRemoteUrl()))
				item3.setSelected(true);
			item3.setOnAction(menuHandler);
			menuServerUrl.getItems().add(item3);

		} else {
			if (menuServerUrl != null) {

			}
		}

	}

	private void initSpringFramework() {
		appContext = new AnnotationConfigApplicationContext(getClass());
		final Environment environment = appContext.getEnvironment();
		String name = environment.getProperty("application.name");
		String version = environment.getProperty("application.version");

		this.primaryStage.setTitle(name);

		final RemoteHolder remoteHolder = appContext.getBean(RemoteHolder.class);

		actionStack = appContext.getBean(ActionStack.class);

		remoteHolder.setRemoteUrl(prefs.get(REMOTE_KEY, remoteHolder.getRemoteUrl()));

		System.out.println(
				String.format("%s %s wurde gestartet mit Remote %s", name, version, remoteHolder.getRemoteUrl()));

	}

	public void showPersonOverview() {

		// Load person overview.
		controller = appContext.getBean(PersonOverviewController.class);

		// Set person overview into the center of root layout.
		rootLayout.setCenter(controller.getView());
	}

	private void initRootLayout() {

		try {
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

				@Override
				public void handle(WindowEvent event) {
					Platform.exit();
					System.exit(0);
				}
			});
			final FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("person/view/RootLayout.fxml"));
			loader.setController(this);
			rootLayout = loader.load();
			Scene scene = new Scene(rootLayout);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		launch(args);
	}
}
