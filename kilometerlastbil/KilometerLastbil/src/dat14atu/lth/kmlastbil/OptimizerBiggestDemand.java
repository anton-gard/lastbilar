package dat14atu.lth.kmlastbil;

public class OptimizerBiggestDemand extends IOptimizer {

	public OptimizerBiggestDemand() {
		super("Biggest Demand First Optimizer");
	}

	@Override
	public void run(Model model, boolean verbose) throws Exception {
		int lastbils[][] = new int[model.getFactories() * model.getStores()][3]; // [factory-store-connection-id][factory, store, demand]
		int id = 0;
		
		// collect
		for(int factory = 0; factory < model.getFactories(); factory++) {
			for(int store = 0; store < model.getStores(); store++) {
				lastbils[id++] = new int[] {
					factory, 
					store, 
					Math.min(model.getStoreDemand(store), model.getFactoryOutput(factory))
				};
			}
		}
		
		//sort
		for(int i = 0; i < lastbils.length - 1; i++) {
			for(int j = i + 1; j < lastbils.length; j++) {
				if(lastbils[i][2] < lastbils[j][2]) {
					int temp[] = lastbils[i];
					lastbils[i] = lastbils[j];
					lastbils[j] = temp;
					
				}
			}
		}
		if(verbose) {
			for(int i = 0; i < lastbils.length; i++) {
				System.out.println("Lastbils #" + i + ": Lastbils=" + lastbils[i][2] + ", Factory=" + lastbils[i][0] + ", Store=" + lastbils[i][1]);
			}
		}
		
		// do stuff
		for(int i = 0; i < lastbils.length; i++) {
			int factory = lastbils[i][0];
			int store = lastbils[i][1];
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
