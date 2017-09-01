package GUI;

import javafx.stage.Stage;
/**
 * FXMLController is common parent Controllers class for FXML 
 * 
 * @author Yew Shao Jie
 */
public  class FXMLController {
	private Stage stage;
	/**
	 * This method set the stage if the controller of corresponding FXML, so that the stage can be access from by abstraction of the parents.
	 * 
	 * @param stage set the JavaFX stage for  controller of corresponding FXML
	 */
	public void setStageAndSetupListeners(Stage stage) {
		this.stage = stage;
	}
	
	/**
	 * @return stage stage of this controller of corresponding FXML
	 */
	public Stage getStage() {
		return stage;
	}
}
