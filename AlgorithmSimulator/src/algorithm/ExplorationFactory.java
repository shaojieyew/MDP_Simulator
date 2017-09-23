package algorithm;

public class ExplorationFactory {
	
	public static final String EX_GREEDY1 = "greedy1";
	public static final String EX_GREEDY2 = "greedy2";
	public static final String EX_WALL1 = "hugwall1";
	public static final String EX_WALL2 =  "hugwall2";
	public static final String EX_WALL3 =  "hugwall3";
	public static  Exploration getInstance (String value){
		if(value.equals(EX_GREEDY1)){
			return new ExplorationType1();
		}
		if(value.equals(EX_GREEDY2)){
			return new ExplorationType2();
		}
		if(value.equals(EX_WALL1)){
			return new ExplorationWallerType1();
		}
		if(value.equals(EX_WALL2)){
			return new ExplorationWallerType2();
		}
		if(value.equals(EX_WALL3)){
			return new ExplorationWallerType3();
		}
		return null;
	}
	

	public static String selectedType =  "hugwall2";
	public static void setSelectedType(String selectedType) {
		ExplorationFactory.selectedType = selectedType;
	}
	public static Exploration getInstance(){
		return getInstance (selectedType);
	}
}
