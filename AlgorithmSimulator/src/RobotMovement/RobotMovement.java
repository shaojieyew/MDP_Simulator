package RobotMovement;

import Data.Robot;

public abstract class RobotMovement implements Runnable{

	public static int count=0;
	private  int id=0;
	public int getId() {
		return id;
	}
	public RobotMovement(){
		this.id = count++;
	}
    private volatile boolean exit = false;
	public boolean isExit() {
		return exit;
	}
	public void setExit(boolean exit) {
		this.exit = exit;
	}
	protected abstract void implementation();
	@Override
	public void run() {
		Robot r = Robot.getInstance();
		r.setMoving(true);
		implementation();
		if(!exit){
			r.runNextInstruction();
		}
	}
    public void stop(){
        exit = true;
    }
}
