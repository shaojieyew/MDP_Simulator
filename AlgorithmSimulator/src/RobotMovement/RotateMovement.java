package RobotMovement;

import Data.Robot;
import javafx.application.Platform;

public class RotateMovement implements Runnable{
	float degree;
	int framePerRotate;
	int milisecondPerRotate;
    private volatile boolean exit = false;
	public RotateMovement(float degree,int framePerRotate,int milisecondPerRotate) {
		super();
		this.degree = degree;
		this.framePerRotate = framePerRotate;
		this.milisecondPerRotate = milisecondPerRotate;
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
		int totalframe = (int) (degree/360f*framePerRotate);
		if(totalframe<=0){
			if(totalframe==0){
				totalframe=1;
			}else{
				totalframe=Math.abs(totalframe);
			}
		}
		float x=degree/(float)totalframe;
		for(int i =0;i<totalframe;i++){
			if(exit){
				break;
			}
			 try {
					Thread.sleep((long) (milisecondPerRotate/framePerRotate));
					Robot.getInstance().setDirection(Robot.getInstance().getDirection()+x);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
		}
		float direction = Math.round(Robot.getInstance().getDirection());
		if(direction<0){
			direction=direction+360;
		}
		r.setDirection(direction);
		r.robotSemaphore.release();
		r.setMoving(false);
	}
    public void stop(){
        exit = true;
    }
}
