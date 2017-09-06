package RobotMovement;

import Data.Robot;
import javafx.application.Platform;

public class RotateMovement extends RobotMovement {
	float degree;
	int framePerRotate;
	int milisecondPerRotate;
	public RotateMovement(float degree,int framePerRotate,int milisecondPerRotate) {
		super();
		this.degree = degree;
		this.framePerRotate = framePerRotate;
		this.milisecondPerRotate = milisecondPerRotate;
	}
	
	@Override
	protected void implementation() {
		//System.out.println("#############ROTATE "+degree+"degree");
		Robot r = Robot.getInstance();
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
				if(isExit()){
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

	}

}
