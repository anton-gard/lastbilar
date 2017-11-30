package dat14atu.lth.kmlastbil;

import java.util.Random;

public class Model {
	
	// model generation limits when using seed
	private final int FACTORY_OUTPUT_MIN = 10;
	private final int FACTORY_OUTPUT_MAX = 100;
	private final int DISTANCE_MAX = 5000;
	
	private int totalLastbils;
	
	private int 
		factoryOutput[], 
		suppliedBy[],
		storeDemand[],
		suppliedTo[],
		distance[][],
		supply[][];
	
	public Model(long seed, int factories, int stores) {
		factoryOutput = new int[factories];
		storeDemand = new int[stores];
		distance = new int[factories][stores];
		supply = new int[factories][stores];
		
		suppliedBy = new int[factories];
		suppliedTo = new int[stores];
		
		generate(seed);
	}
	
	public Model(int factories, int stores, int[] production, int[] demand, int[][] distances) {
		
		factoryOutput = new int[factories];
		storeDemand = new int[stores];
		distance = new int[factories][stores];
		
		for(int factory = 0; factory < factories; factory++) {
			factoryOutput[factory] = production[factory];
			totalLastbils += factoryOutput[factory];
		}
		
		for(int store = 0; store < stores; store++) {
			storeDemand[store] = demand[store];
		}

		for(int factory = 0; factory < factories; factory++) {
			for(int store = 0; store < stores; store++) {
				distance[factory][store] = distances[factory][store];
			}
		}
		
		supply = new int[factories][stores];
		suppliedBy = new int[factories];
		suppliedTo = new int[stores];
	}
	
	public static int getKilometerLastbils(Model model) {
		int kml = 0;
		for(int factory = 0; factory < model.factoryOutput.length; factory++) {
			for(int store = 0; store < model.storeDemand.length; store++) {
				kml += model.distance[factory][store] * model.supply[factory][store];
			}
		}
		return kml;
	}
	public int getKilometerLastbils() {
		return getKilometerLastbils(this);
	}
	
	public int getFactories() {
		return factoryOutput.length;
	}	
	public int getFactoryOutput(int factory) {
		return factoryOutput[factory];
	}
	public int getFactoryRemainingOutput(int factory) {
		return getFactoryOutput(factory) - suppliedBy[factory];
	}
	
	public int getStores() {
		return storeDemand.length;
	}
	public int getStoreDemand(int store) {
		return storeDemand[store];
	}
	public int getStoreRemainingDemand(int store) {
		return getStoreDemand(store) - suppliedTo[store];
	}
	
	public int getDistance(int factory, int store) {
		return distance[factory][store];
	}
	
	public void setSupply(int factory, int store, int lastbilar) {
		setSupply(factory, store, lastbilar, false);
	}
	public void setSupply(int factory, int store, int lastbilar, boolean suppressWarning) {
		if(lastbilar > getFactoryRemainingOutput(factory) + supply[factory][store]) {
			if(!suppressWarning) {
				System.out.println("Warning|Model.setSupply:ExceedingOutput| - The number of Lastbils supplied cannot exceed the output of the specified factory.");
			}
			lastbilar = getFactoryRemainingOutput(factory) + supply[factory][store];
		}
		if(lastbilar > getStoreRemainingDemand(store) + supply[factory][store]) {
			if(!suppressWarning) {
				System.out.println("Warning|Model.setSupply:ExceedingDemand| - The number of Lastbils supplied cannot exceed the demand of the specified store.");
			}
			lastbilar = getStoreRemainingDemand(store) + supply[factory][store];
		}
		if(lastbilar < 0) {
			if(!suppressWarning) {
				System.out.println("Warning|Model.setSupply:SupplyBelowZero| - The number of Lastbils supplied cannot be lower than 0.");
			}
			lastbilar = 0;
		}
		int change = lastbilar - supply[factory][store];
		supply[factory][store] += change;
		suppliedBy[factory] += change;
		suppliedTo[store] += change;
	}
	
	public int getSupply(int factory, int store) {
		return supply[factory][store];
	}
	
	public boolean verify(boolean verbose) {
		boolean passed = true;
		
		int suppliedByFactory[] = new int[factoryOutput.length];
		int suppliedToStore[] = new int[storeDemand.length];
		
		for(int factory = 0; factory < factoryOutput.length; factory++) {
			int supplied = 0;
			for(int store = 0; store < storeDemand.length; store++) {
				supplied += supply[factory][store];
			}
			suppliedByFactory[factory] = supplied;
			if(factoryOutput[factory] != suppliedByFactory[factory]) {
				passed = false;
			}
		}
		
		for(int store = 0; store < storeDemand.length; store++) {
			int supplied = 0;
			for(int factory = 0; factory < factoryOutput.length; factory++) {
				supplied += supply[factory][store];
			}
			suppliedToStore[store] = supplied;
			if(suppliedToStore[store] != storeDemand[store]) {
				passed = false;
			}
		}
		
		if(verbose) {
			System.out.println();
			System.out.println("-----Model Verification-----");
			
			int factoriesPassed = 0;
			for(int factory = 0; factory < suppliedByFactory.length; factory++) {
				String s = "Factory #" + factory + " ";
				if(factoryOutput[factory] != suppliedByFactory[factory]) {
					s += "failed";
				}
				else {
					s += "passed";
					factoriesPassed++;
				}
				s += " with " + suppliedByFactory[factory] + " of " + factoryOutput[factory] + " Lastbils supplied.";
				System.out.println(s);
			}
			
			int storesPassed = 0;
			for(int store = 0; store < suppliedToStore.length; store++) {
				String s = "Store #" + store + " ";
				if(storeDemand[store] != suppliedToStore[store]) {
					s += "failed";
				}
				else {
					s += "passed";
					storesPassed++;
				}
				s += " with " + suppliedToStore[store] + " of " + storeDemand[store] + " Lastbils supplied.";
				System.out.println(s);
			}
			
			System.out.println();
			System.out.println("Factory verification: \t" + factoriesPassed + "/" + factoryOutput.length + " factories passed.");
			System.out.println("Store verification: \t" + storesPassed + "/" + storeDemand.length + " stores passed.");
		}
		
		return passed;
	}

	public void printMe() {
		int columns = getStores() + 1;
		String format = new String(new char[columns]).replace("\0", "%10s") + "\n";
		Object row[] = new String[columns];
		row[0] = "FAC\\STORE";
		for(int store = 0; store < getStores(); store++) {
			row[1 + store] = "" + getStoreDemand(store);
		}
		System.out.format(format, row);
		
		row = new String[columns];
		for(int factory = 0; factory < getFactories(); factory++) {
			row[0] = "" + getFactoryOutput(factory);
			for(int store = 0; store < getStores(); store++) {
				row[1 + store] = "" + getSupply(factory, store);
			}
			System.out.format(format, row);
		}
	}
	
	private void generate(long seed) {
		Random r = new Random(seed);
		
		// create supply
		totalLastbils = 0;
		for(int i = 0; i < factoryOutput.length; i++) {
			int output = r.nextInt(FACTORY_OUTPUT_MAX - FACTORY_OUTPUT_MIN) + FACTORY_OUTPUT_MIN;
			totalLastbils += output;
			factoryOutput[i] = output;
		}
		
		// create relative demand
		int totalDemand = 0;
		for(int i = 0; i < storeDemand.length; i++) {
			int demand = r.nextInt(100) + 1;
			totalDemand += demand;
			storeDemand[i] = demand;
		}
		
		// adjust demand for supply
		int newDemand = 0;
		for(int i = 0; i < storeDemand.length - 1; i++) {
			int demandAdjusted = (int) (totalLastbils * (1.0 * storeDemand[i]) / (1.0 * totalDemand));
			newDemand += demandAdjusted;
			storeDemand[i] = demandAdjusted;
		}
		storeDemand[storeDemand.length - 1] = totalLastbils - newDemand;
		
		// create distances
		for(int i = 0; i < distance.length; i++) {
			for(int j = 0; j < distance[0].length; j++) {
				distance[i][j] = r.nextInt(DISTANCE_MAX);
			}
		}
	}
}
