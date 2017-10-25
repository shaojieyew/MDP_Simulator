package algorithm;

public class ExplorationFactory {
	
	public static final String EX_GREEDY1 = "greedy1";
	public static final String EX_GREEDY2 = "greedy2";
	public static final String EX_WALL1 = "hugwall1";
	public static final String EX_WALL2 =  "hugwall2";
	public static final String EX_WALL2R =  "hugwall2r";
	public static final String EX_WALL3 =  "hugwall3";
	public static final String EX_WALL4 =  "hugwall4";
	public static final String EX_WALL6 = "hugwall6";
	public static final String EX_WALL5 = "hugwall5";
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
		if(value.equals(EX_WALL2R)){
			return new ExplorationWallerType2R();
		}
		if(value.equals(EX_WALL3)){
			return new ExplorationWallerType3();
		}
		if(value.equals(EX_WALL4)){
			return new ExplorationWallerType4();
		}
		if(value.equals(EX_WALL5)){
			return new ExplorationWallerType5();
		}
		if(value.equals(EX_WALL6)){
			return new ExplorationWallerType6();
		}
		return null;
	}
	

	public static String selectedType =  "hugwall2";
	public static void setSelectedType(String selectedType) {
		ExplorationFactory.selectedType = selectedType;
	}
	public static String getSelectedType() {
		return selectedType;
	}
	public static Exploration getInstance(){
		return getInstance (selectedType);
	}
}
