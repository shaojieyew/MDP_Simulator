package algorithm;

public class FastestPathFactory {

	public static final String FP1 = "fp1";
	public static final String FP2 = "fp2";
	public static final String FP3 = "fp3";
	public static  FastestPath getInstance (String value){
		if(value.equals(FP1)){
			return new FastestPathType1();
		}
		if(value.equals(FP2)){
			return new FastestPathType2();
		}
		if(value.equals(FP3)){
			return new FastestPathType3();
		}
			return null;
	}
	

	public static String selectedType =  "fp3";
	public static void setSelectedType(String selectedType) {
		FastestPathFactory.selectedType = selectedType;
	}
	public static FastestPath getInstance(){
		return getInstance (selectedType);
	}
}
