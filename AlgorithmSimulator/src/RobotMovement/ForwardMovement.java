package RobotMovement;

import java.text.DecimalFormat;

import Data.Robot;
import javafx.application.Platform;

public class ForwardMovement extends RobotMovement {
	float distance;
	int framePer10CM;
	int milisecondPer10CM;
	public ForwardMovement(float distance, int framePer10CM, int milisecondPer10CM) {
		super();
		this.distance=distance;
		this.framePer10CM = framePer10CM;
		this.milisecondPer10CM = milisecondPer10CM;
	}

	@Override
	protected void implementation() {
		Robot r = Robot.getInstance();
		float direction = r.getDirection()%360;
		//System.out.println("forward "+distance+"cm" +" facing "+direction);

		//System.out.println("#############MOVE_FORWARD_ "+distance);
	    double radians = Math.toRadians(direction);
		float moveX =  ((distance/10f)*(float)Math.sin(radians));
		float moveY =  ((distance/10f)*(float)Math.cos(radians));
		
		float dist = (float) Math.sqrt(Math.pow((moveX), 2)+Math.pow((moveY),2));
		int totalframe = (int) (dist*framePer10CM);
		float x=moveX/(float)totalframe;
		float y=moveY/(float)totalframe;
		for(int i =0;i<totalframe;i++){
			if(isExit()){
				break;
			}
	        try {
				Thread.sleep(milisecondPer10CM/framePer10CM);
	                	 r.setPosX(r.getPosX()+x);
	                	 r.setPosY(r.getPosY()+y);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		DecimalFormat df = new DecimalFormat("#.##");
	   	r.setPosX(Float.parseFloat(df.format(r.getPosX())));
	   	r.setPosY(Float.parseFloat(df.format(r.getPosY())));

	}

}
