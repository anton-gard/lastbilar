package dat14atu.lth.kmlastbil;

public class Link {
	private Model model;
	
	private int factory;
	private int toStore, fromStore;
	
	private int distanceGain;
	protected int getDistanceGain() {
		return distanceGain;
	}
	
	protected int getMaxSupplyFlow() {
		return Math.min(
				model.getStoreDemand(toStore) - model.getSupply(factory, toStore),
				model.getSupply(factory, fromStore)
			);
	}
	
	protected int getFactory() {
		return factory;
	}
	protected int getStoreTo() {
		return toStore;
	}
	protected int getStoreFrom() {
		return fromStore;
	}
	
	protected Link(Model model, int factory, int toStore, int fromStore) {
		distanceGain = model.getDistance(factory, fromStore) - model.getDistance(factory, toStore);
		this.model = model;
		this.factory = factory;
		this.toStore = toStore;
		this.fromStore = fromStore;
	}
	
	protected int invokePull(int desiredSupplyFlow) {
		int realPull = Math.min(getMaxSupplyFlow(), desiredSupplyFlow);
		model.setSupply(factory, fromStore, model.getSupply(factory, fromStore) - realPull);
		
		return realPull;
	}
	
	protected int invokePush(int desiredSupplyFlow) {
		int realPush = Math.min(model.getFactoryRemainingOutput(factory), desiredSupplyFlow);
		realPush = Math.min(realPush, model.getStoreRemainingDemand(toStore));
		model.setSupply(factory, toStore, model.getSupply(factory, toStore) + realPush); 
		
		return realPush;
	}
	
	protected Link clone() {
		return new Link(model, factory, toStore, fromStore);
	}
	
	@Override
	public String toString() {
		return "[S(" + getStoreTo() + ")F(" + getFactory() + ")S(" + getStoreFrom() + ")]";
	}
}
