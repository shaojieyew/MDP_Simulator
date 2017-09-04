package GUI;

import Data.Map;
import Data.MapListener;
import Data.Position;
import Data.Robot;
import Data.RobotListener;
import Data.WayPoint;
import Data.WayPointListener;
import RPiInterface.RobotSensorSimulator;
import RPiInterface.RobotSensorSimulatorFactory;
import algorithm.Test;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class MapObstacleGUI extends MapGUI{

    EventHandler<MouseEvent> mouseEventListener;
	@Override
	protected void loadMap(){
		for(int x =0;x<15;x++){
			for(int y=0;y<20;y++){
				getChildren().remove(tiles[y][x]);
				if(tiles[y][x]==null){
					tiles[y][x] = new Rectangle();
					tiles[y][x].widthProperty().bind(widthProperty().divide(15));
					tiles[y][x].heightProperty().bind(heightProperty().divide(20));
					tiles[y][x].xProperty().bind(widthProperty().divide(15).multiply(x));
					tiles[y][x].yProperty().bind(heightProperty().subtract(heightProperty().divide(20).multiply(y+1)));
				}
				int obstacleValue = Map.getInstance().getObstacles()[y][x];
				if(obstacleValue==1){
						tiles[y][x].setFill(Color.BLACK);
				}else{
						tiles[y][x].setFill(Color.WHITE);
				}
				getChildren().add(tiles[y][x]);
			}
		}

		if(mouseEventListener!=null){
			this.removeEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventListener);
			this.removeEventFilter(MouseEvent.MOUSE_PRESSED, mouseEventListener);
			this.removeEventFilter(MouseEvent.MOUSE_DRAGGED, mouseEventListener);
		}
        mouseEventListener = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
            	setObstacle(mouseEvent);
            }
        };
		this.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventListener);
		this.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEventListener);
		this.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseEventListener);
		
		loadStartEnd();
		loadWayPoint();
	}
	
	private void setObstacle(MouseEvent mouseEvent){
		Double cellWidth = widthProperty().divide(15).get();
    	Double cellHeight = heightProperty().divide(20).get();
    	int x = (int) ((mouseEvent.getX()-(mouseEvent.getX()%cellWidth))/cellWidth);
    	int y =19-(int) ((mouseEvent.getY()-(mouseEvent.getY()%cellHeight))/cellHeight);
    	if(x<0||y<0||x>14||y>19){
    		return;
    	}
    	Map m = Map.getInstance();
    	int[][] obstacles = m.getObstacles();
    	if(mouseEvent.getButton().compareTo(MouseButton.PRIMARY)==0){
    		obstacles[y][x]=1;
    	}else{
        	obstacles[y][x]=0;
    	}
    	m.setObstacle(obstacles);
	}
}
