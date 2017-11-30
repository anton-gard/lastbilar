package dat14atu.lth.kmlastbil;

public class OptimizerShortestDistance extends IOptimizer {
	
	public OptimizerShortestDistance() {
		super("Shortest Distance First Optimizer");
	}
	
	@Override
	public void run(Model model, boolean verbose) throws Exception {
		int distances[][] = new int[model.getFactories() * model.getStores()][3]; // [factory-store-connection-id][factory, store, distance]
		int id = 0;
		
		// collect
		for(int factory = 0; factory < model.getFactories(); factory++) {
			for(int store = 0; store < model.getStores(); store++) {
				distances[id++] = new int[] {
					factory, 
					store, 
					model.getDistance(factory, store)
				};
			}
		}
		
		//sort ascending
		for(int i = 0; i < distances.length - 1; i++) {
			for(int j = i + 1; j < distances.length; j++) {
				if(distances[i][2] > distances[j][2]) {
					int temp[] = distances[i];
					distances[i] = distances[j];
					distances[j] = temp;
					
				}
			}
		}
		
		// do stuff
		for(int i = 0; i < distances.length; i++) {
			int factory = distances[i][0];
			int store = distances[i][1];
			int output = model.getFactoryRemainingOutput(factory);
			int demand = model.getStoreRemainingDemand(store);
			int supply = 0;
			if(output >= demand && demand > 0) {
				supply = demand;
			}
			else if(demand > output && output > 0) {
				supply = output;
			}
			model.setSupply(factory, store, supply);
		}

		super.run(model, verbose);
	}
}
