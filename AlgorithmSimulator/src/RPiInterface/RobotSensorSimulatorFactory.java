package RPiInterface;

public class RobotSensorSimulatorFactory {

	public static final String SENSOR_TYPE_1 = "type1";
	public static final String SENSOR_TYPE_2 = "type2";
	public static  RobotSensorSimulator getInstance (String value){
		if(value.equals(SENSOR_TYPE_1)){
			return new RobotSensorSimulatorType1();
		}
		if(value.equals(SENSOR_TYPE_2)){
			return new RobotSensorSimulatorType2();
		}
			return null;
	}
}
