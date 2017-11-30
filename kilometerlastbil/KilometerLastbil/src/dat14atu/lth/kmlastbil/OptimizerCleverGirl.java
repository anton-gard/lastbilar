package dat14atu.lth.kmlastbil;

import java.util.ArrayList;
import java.util.List;

public class OptimizerCleverGirl extends IOptimizer {
	
	// settings
	private static final int NORMAL_DERP_LIMIT = 7;
	private static final int PERFECTIONIST_DERP_LIMIT = 50;
	
	private boolean perfectionist;
	private IOptimizer distributor;
	private Model model;
	
	public OptimizerCleverGirl(IOptimizer initialDistributor, boolean perfectionist) {
		super((perfectionist ? "Perfectionist" : "") + "Equimizer (" + initialDistributor.getName() + ")");
		distributor = initialDistributor;
		this.perfectionist = perfectionist;
	}
	
	@Override
	public void run(Model model, boolean verbose) throws Exception {
		this.model = model;
		
		// start with an even distribution
		distributor.run(model, false);
		
		// be clever girl
		int derp = 0; // if the optimizer is derping out
		boolean equilibrium = false;
		do {
			int chaos = 0;
			for(int factory = 0; factory < model.getFactories(); factory++) {
				for(int toStore = 0; toStore < model.getStores(); toStore++) {
					for(int fromStore = 0; fromStore < model.getStores(); fromStore++) {
						if(toStore == fromStore) {
							continue;
						}
						//System.out.println(factory + ": " + toStore + "(" + model.getDistance(factory, toStore) + ") <- " + fromStore
						//		+ "(" + model.getDistance(factory, fromStore) + ")");
						
						if(perfectionist || model.getDistance(factory, toStore) < model.getDistance(factory, fromStore)) {
							int desiredFlow = Integer.MAX_VALUE; // w/e
							int realizedFlow = doMagic(factory, fromStore, toStore, desiredFlow);
							chaos += realizedFlow;
						}
					}
				}
			}
			equilibrium = chaos == 0;
		}
		while(!equilibrium && derp++ <= (perfectionist ? PERFECTIONIST_DERP_LIMIT : NORMAL_DERP_LIMIT)); // equilibrium or derp limit reached
		
		super.run(model, verbose);
	}
	
	private int doMagic(int factory, int fromStore, int toStore, int desiredFlow) throws Exception {
		
		List<SupplyChain> supplyChains = new ArrayList<SupplyChain>();
		
		// retrieve magic
		SupplyChain baseChain = new SupplyChain(model, new Link(model, factory, fromStore, toStore));
		for(int factoryNext = 0; factoryNext < model.getFactories(); factoryNext++) {
			if(factoryNext == factory) {
				continue;
			}
			for(int fromStoreNext = 0; fromStoreNext < model.getStores(); fromStoreNext++) {
				if(fromStoreNext == fromStore) {
					continue;
				}
				
				List<SupplyChain> chainsAdd = doRecursiveMagic(baseChain.clone(), factoryNext, fromStoreNext);
				if(chainsAdd != null) {
					for(SupplyChain chainAdd : chainsAdd) {
						if(chainAdd == null) {
							continue;
						}
						supplyChains.add(chainAdd);
					}
				}
			}
		}
		
		// sort the magic in descending distance gain
		supplyChains.sort((SupplyChain chainA, SupplyChain chainB) -> {
			return chainB.getTotalDistanceGain() - chainA.getTotalDistanceGain();
		});
		
		// execute magic
		int totalFlow = 0;
		for(SupplyChain chain : supplyChains) {
			int effectivePull = chain.invokePull(desiredFlow - totalFlow);
			int effectivePush = chain.invokePush(effectivePull);
			if(effectivePull != effectivePush) {
				System.out.println("Warning|OptimizerCleverGirl.doMagic:PullPushDisjoint| - The effective push and pull of a supply chain does not match!");
			}
			totalFlow += effectivePush;
			
			if(totalFlow >= desiredFlow) {
				break;
			}
		}
		
		return totalFlow;
	}
	
	private List<SupplyChain> doRecursiveMagic(SupplyChain chain, int factory, int storeFrom) throws Exception {
		
		int storeTo = chain.getLastLink().getStoreFrom();
		
		// fuck-my-shit-up-checks
		if(	chain.containsPull(factory, storeTo) ||
			chain.containsPull(factory, storeFrom) ||
			chain.containsPush(factory, storeTo) ||
			chain.containsPush(factory, storeFrom)) {
			return null;
		}
		
		boolean endOfLine = chain.addLink(new Link(model, factory, storeTo, storeFrom));
		
		// base case baby
		if(endOfLine) {
			if(chain.getTotalDistanceGain() > 0) {
				List<SupplyChain> chains = new ArrayList<SupplyChain>();
				chains.add(chain);
				return chains;
			}
			else {
				return null;
			}
		}
		
		List<SupplyChain> supplyChains = new ArrayList<SupplyChain>();
		for(int factoryNext = 0; factoryNext < model.getFactories(); factoryNext++) {
			if(factoryNext == factory) {
				continue;
			}
			
			// I think this is right. WHYYY DOOO
			if(chain.containsPull(factoryNext, storeTo) || chain.containsPush(factoryNext, storeTo)) {
				continue;
			}
			
			for(int storeFromNext = 0; storeFromNext < model.getStores(); storeFromNext++) {
				if(storeFromNext == storeFrom) {
					continue;
				}
				
				// I think this is also right THEEESSEE GUUUYYSSS
				if(chain.containsPush(factoryNext, storeFromNext)) {
					continue;
				}
				
				// Uncertain of this one, think it's right though NOOOOTTT WOOORRRKKKKK ???
				if(chain.containsPull(factoryNext, storeFromNext)) {
					continue;
				}
				
				if(!perfectionist) {
					if(model.getDistance(factoryNext, storeFrom) > model.getDistance(factoryNext, storeFromNext)) {
						continue;
					}
				}
				
				SupplyChain chainClone = chain.clone();
				List<SupplyChain> chainsAdd = doRecursiveMagic(chainClone, factoryNext, storeFromNext);
				if(chainsAdd != null) {
					for(SupplyChain chainAdd : chainsAdd) {
						if(chainAdd != null) {
							if(chainAdd.getTotalDistanceGain() > 0) {
								supplyChains.add(chainAdd);
							}
						}
					}
				}
			}
		}
		
		return supplyChains.size() > 0 ? supplyChains : null;
	}
}
