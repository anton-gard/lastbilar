package dat14atu.lth.kmlastbil;

public abstract class IOptimizer {
	
	private String name;
	
	public IOptimizer(String name) {
		this.name = name;
	}
	
	public void run(Model model, boolean verbose) throws Exception{
		if(verbose) {
			model.printMe();
		}
	}
	
	public String getName() {
		return name;
	}
}
