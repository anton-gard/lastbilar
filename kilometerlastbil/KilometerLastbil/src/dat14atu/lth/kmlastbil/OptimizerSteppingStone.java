package dat14atu.lth.kmlastbil;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OptimizerSteppingStone extends IOptimizer {
	
	IOptimizer distributor;
	Model model;
	int supplyTable[][];
	
	public OptimizerSteppingStone(IOptimizer initialDistributor) {
		super("Stepping Stone (" + initialDistributor.getName() + ")");
		
		distributor = initialDistributor;
	}
	
	@Override
	public void run(Model model, boolean verbose) throws Exception {

		supplyTable = new int[model.getFactories()][model.getStores()];
		this.model = model;
		distributor.run(model, verbose);
		
		List<Point> emptyCells = new ArrayList<Point>();
		int occupied = 0;
		for(int factory = 0; factory < model.getFactories(); factory++) {
			for(int store = 0; store < model.getStores(); store++) {
				supplyTable[factory][store] = model.getSupply(factory, store);
				if(supplyTable[factory][store] != 0) {
					occupied++;
				}
				else {
					emptyCells.add(new Point(factory, store));
				}
			}
		}
		if(occupied != model.getFactories() + model.getStores() - 1) {
			if(verbose) {
				System.out.println("Distribution is degenerate, let's see what happens...");
			}
			//return ;
		}
		
		for(Point cell : emptyCells) {
			List<Point> path = new ArrayList<Point>();
			path.add(cell);
			path = recursePath(path, true);
			if(path == null) {
				continue;
			}
			
			
		}
		
		super.run(model, verbose);
	}
	
	private List<Point> recursePath(List<Point> history, boolean moveIsHorizontal) {
		Point lastPosition = history.get(history.size() - 1);
		List<Point> bestPath = null;
		int bestPathPerformance = 0;
		
		// move through stores
		if(moveIsHorizontal) {
			for(int store = 0; store < model.getStores(); store++) {
				if(store == lastPosition.y) {
					continue;
				}
				if(lastPosition.x == history.get(0).x && store == history.get(0).y) {
					bestPath = history;
					break;
				}
				if(supplyTable[lastPosition.x][store] != 0) {
					List<Point> newPath = new ArrayList<Point>();
					Collections.copy(newPath, history);
					newPath = recursePath(newPath, !moveIsHorizontal);
					
					if(bestPath != null) {
						
					}
				}
				
			}
		}
		
		// move through factories
		else {
			
		}
		
		return bestPath;
	}
	
	private int evaluatePath(List<Point> path) {
		return 0;
	}
}
