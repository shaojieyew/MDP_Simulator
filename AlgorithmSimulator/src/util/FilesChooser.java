package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class FilesChooser {
	public static final ExtensionFilter FORMAT_TEXT = new FileChooser.ExtensionFilter("Text (.txt)", "*.txt");

	public static File show(Stage stage, String title, String initialLocation, ExtensionFilter filter){
		File init = null;
		if(initialLocation!=null)
			init = new File(initialLocation);
		if(!init.exists()){
			init = new File(Paths.get("").toAbsolutePath().toString());
		}
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(init);
		fileChooser.setTitle(title);
	    fileChooser.getExtensionFilters().addAll(filter);
		return fileChooser.showOpenDialog(stage);
	}
	public static File show(Stage stage, String title, String initialLocation, ExtensionFilter filters[]){
		File init = null;
		if(initialLocation!=null)
			init = new File(initialLocation);
		if(!init.exists()){
			init = new File(Paths.get("").toAbsolutePath().toString());
		}
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(init);
		fileChooser.setTitle(title);
	    fileChooser.getExtensionFilters().addAll(filters);
		return fileChooser.showOpenDialog(stage);
	}

	public static File save(Stage stage, String title, String initialLocation, ExtensionFilter filters){
		File init = null;
		if(initialLocation!=null)
			init = new File(initialLocation);
		if(!init.exists()){
			init = new File(Paths.get("").toAbsolutePath().toString());
		}
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(init);
		fileChooser.setTitle(title);
	    fileChooser.getExtensionFilters().addAll(filters);
		return fileChooser.showSaveDialog(stage);
	}
}
