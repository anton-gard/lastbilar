package dat14atu.lth.kmlastbil;

import java.awt.Point;

public class SteppingStone {
	
	private int table[][];
	
	protected SteppingStone(Model model, Point start) {
		
		table = new int[model.getFactories()][model.getStores()];
		for(int factory = 0; factory < model.getFactories(); factory++) {
			for(int store = 0; store < model.getStores(); store++) {
				table[factory][store] = model.getSupply(factory, store);
			}
		}
		
	}
	
	protected void reset() {
		
	}
}
