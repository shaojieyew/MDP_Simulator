package RobotMovement;

import java.text.DecimalFormat;

import Data.Robot;
import javafx.application.Platform;

public class ForwardMovement implements Runnable{
	float moveX;
	float moveY;
	int framePer10CM;
	int milisecondPer10CM;
    private volatile boolean exit = false;
	public ForwardMovement(float moveX, float moveY, int framePer10CM, int milisecondPer10CM) {
		super();
		this.moveX = moveX;
		this.moveY = moveY;
		this.framePer10CM = framePer10CM;
		this.milisecondPer10CM = milisecondPer10CM;
	}
	@Override
	public void run() {
		 Robot r = Robot.getInstance();
		try {
			r.robotSemaphore.acquire();
			r.setMoving(true);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		float dist = (float) Math.sqrt(Math.pow((moveX), 2)+Math.pow((moveY),2));
		int totalframe = (int) (dist*framePer10CM);
		float x=moveX/(float)totalframe;
		float y=moveY/(float)totalframe;
		for(int i =0;i<totalframe;i++){
			if(exit){
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
		Robot.getInstance().robotSemaphore.release();
		r.setMoving(false);
	}
    public void stop(){
        exit = true;
    }
}
