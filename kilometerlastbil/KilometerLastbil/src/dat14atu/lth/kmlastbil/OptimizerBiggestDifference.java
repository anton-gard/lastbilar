package dat14atu.lth.kmlastbil;

public class OptimizerBiggestDifference extends IOptimizer {

	public OptimizerBiggestDifference() {
		super("Biggest Difference First Optimizer");
	}
	
	@Override
	public void run(Model model, boolean verbose) throws Exception {
		
		int distances[][] = new int[model.getFactories()][model.getStores()];
		
		// [index][difference, factory, storeA, storeB]
		int differences[][] = new int[model.getFactories() * additorial(model.getStores())][4];
		
		// collect distances
		for(int factory = 0; factory < model.getFactories(); factory++) {
			for(int store = 0; store < model.getStores(); store++) {
				distances[factory][store] = model.getDistance(factory, store);
			}
		}
		
		// compare differences
		int index = 0;
		for(int factory = 0; factory < model.getFactories(); factory++) {
			for(int storeA = 0; storeA < model.getStores() - 1; storeA++) {
				for(int storeB = storeA + 1; storeB < model.getStores(); storeB++) {
					if(storeA == storeB) {
						continue;
					}
					differences[index++] = new int[] {
							Math.abs(distances[factory][storeA] - distances[factory][storeB]),
							factory,
							storeA,
							storeB
					};
				}
			}
		}
		
		// sort differences descending
		for(int diffA = 0; diffA < differences.length - 1; diffA++) {
			for(int diffB = diffA + 1; diffB < differences.length; diffB++) {
				if(differences[diffA][0] < differences[diffB][0]) {
					int temp[] = differences[diffA];
					differences[diffA] = differences[diffB];
					differences[diffB] = temp;
				}
			}
		}
		
		if(verbose) {
			System.out.println(getName() + ": Sorted distance differences.");
		}
		for(int diff[] : differences) {
			if(verbose) {
				System.out.println("Distance difference " + diff[0] + " between stores " + diff[2] + " and " + diff[3] + " from factory " + diff[1]);
			}
			
			int factory = diff[1];
			int storeA = diff[2];
			int storeB = diff[3];
			
			// allocate optimally (we hope)
			int nearestStore = model.getDistance(factory, storeA) < model.getDistance(factory, storeB) ? storeA : storeB;
			int available = Math.min(model.getFactoryRemainingOutput(factory), model.getStoreRemainingDemand(nearestStore));
			available +=  + model.getSupply(factory, nearestStore);
			model.setSupply(factory, nearestStore, available);
			
			if(verbose) {
				System.out.println("Sending " + available + "/" + model.getFactoryOutput(factory) + " from factory " + factory + 
						" to store " + nearestStore + " " + available + "/" + model.getStoreDemand(nearestStore));
			}
		}
		
		// fill in the shitter
		for(int factory = 0; factory < model.getFactories(); factory++) {
			for(int store = 0; store < model.getStores(); store++) {
				int fill = Math.min(model.getFactoryRemainingOutput(factory), model.getStoreRemainingDemand(store));
				if(fill > 0) {
					model.setSupply(factory, store, fill + model.getSupply(factory, store));
					//System.out.println("SHITTER");
				}
			}
		}
		
		super.run(model, verbose);
	}
	
	private int additorial(int n) {
		int sum = 0;
		for(int add = 1; add < n; add++) {
			sum += add;
		}
		return sum;
	}
}
