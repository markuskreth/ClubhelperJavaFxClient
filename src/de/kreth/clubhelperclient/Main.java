package de.kreth.clubhelperclient;

import org.controlsfx.dialog.ExceptionDialog;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import de.kreth.clubhelperclient.person.view.PersonOverviewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

@Configuration
@ComponentScan("de.kreth.clubhelperclient")
@PropertySource("classpath:application.properties")
public class Main extends Application {

	private ApplicationContext appContext;
	private Stage primaryStage;
	private BorderPane rootLayout;

	@Override
	public void start(Stage primaryStage) {
		try {
			this.primaryStage = primaryStage;
			this.primaryStage.setTitle("Clubhelper App");

			initSpringFramework();

			initRootLayout();

			showPersonOverview();
		} catch (Exception e) {
			ExceptionDialog dlg = new ExceptionDialog(e);
			dlg.show();
		}
	}

	private void initSpringFramework() {
		appContext = new AnnotationConfigApplicationContext(getClass());
		String name = appContext.getEnvironment().getProperty("application.name");
		String version = appContext.getEnvironment().getProperty("application.version");
		System.out.println(String.format("%s %s wurde gestartet", name, version));
	}

	public void showPersonOverview() {

		// Load person overview.
		PersonOverviewController controller = appContext.getBean(PersonOverviewController.class);

		// Set person overview into the center of root layout.
		rootLayout.setCenter(controller.getView());
	}

	private void initRootLayout() {

		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("person/view/RootLayout.fxml"));
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
