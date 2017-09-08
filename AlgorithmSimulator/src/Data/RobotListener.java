package Data;

public interface RobotListener {
	public  void updateRobot();
	public  void onRobotStop();
	public  void onRobotStartExploring();
	public  void onRobotStopExploring();
}
