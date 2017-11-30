package dat14atu.lth.kmlastbil;

public class OptimizerEvenDistribution extends IOptimizer {

	public OptimizerEvenDistribution() {
		super("Even Distribution \"Optimizer\"");
	}
	
	@Override
	public void run(Model model, boolean verbose) throws Exception {

		int produce[] = new int[model.getFactories()];
		for(int factory = 0; factory < model.getFactories(); factory++) {
			produce[factory] = model.getFactoryOutput(factory);
		}
		int totalDemand = 0;
		int demand[] = new int[model.getStores()];
		for(int store = 0; store < model.getStores(); store++) {
			demand[store] = model.getStoreDemand(store);
			totalDemand += demand[store];
		}
		
		// evenly distribute all the things
		for(int factory = 0; factory < model.getFactories(); factory++) {
			for(int store = 0; store < model.getStores() - 1; store++) {
				int supply = (int) Math.ceil(produce[factory] * demand[store] * 1.0 / (1.0 * totalDemand));
				supply = Math.min(Math.min(model.getFactoryRemainingOutput(factory), model.getStoreRemainingDemand(store)), supply);
				model.setSupply(factory, store, supply, !verbose);
			}
			// last store get remaining stuff or until demand is met
			model.setSupply(factory, model.getStores() - 1, model.getFactoryRemainingOutput(factory), !verbose);
		}
		
		// fill in the gaps
		for(int factory = 0; factory < model.getFactories(); factory++) {
			int remainingOutput = model.getFactoryRemainingOutput(factory);
			if(remainingOutput > 0) {
				for(int store = 0; store < model.getStores(); store++) {
					int remainingDemand = model.getStoreRemainingDemand(store);
					if(remainingDemand > 0) {
						int supply = model.getSupply(factory, store);
						supply += Math.min(remainingDemand, remainingOutput);
						model.setSupply(factory, store, supply, !verbose);
					}
				}
			}
		}
		
		super.run(model, verbose);
	}
}
