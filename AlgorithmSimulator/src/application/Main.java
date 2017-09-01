package application;
	
import GUI.FXMLController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			//Load FXML Main.fxml and setup controller of FXML
            FXMLLoader mainLoader = new FXMLLoader();
            mainLoader.setLocation(FXMLController.class.getResource("Main.fxml"));
            BorderPane mainLayout = (BorderPane) mainLoader.load();

            FXMLController controller = (FXMLController)mainLoader.getController();
            controller.setStageAndSetupListeners(primaryStage); 
            
			//Setup CSS Style for the FXML
			Scene scene = new Scene(mainLayout);
			primaryStage.setScene(scene);
			primaryStage.setTitle("MDP 14 Simulator");
			//Show the stage; application window
			primaryStage.show();
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	            @Override
	            public void handle(WindowEvent t) {
	                Platform.exit();
	                System.exit(0);
	            }
			});

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
