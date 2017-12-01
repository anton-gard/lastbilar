package dat14atu.lth.kmlastbil;

import java.util.Locale;

public class Main {
	
	// settings
	final static boolean VERBOSE = false;
	final static long STARTING_SEED = 0;
	final static double SCORE_IS_TIE = 0.0000001;
	final static int PRINT_INTERVAL = 1000000;
	final static int ROUNDS = 1000000;
	
	// model
	final static boolean CUSTOM = false;
	final static int FACTORY_COUNT = 3;
	final static int STORE_COUNT = 8;
	
	private static IOptimizer optimizers[];
	private static double[][] scores;
	private static long[][] timings;
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		// våra siffror, körs om CUSTOM=true
		int customFactories = 3;
		int customStores = 8;
		int customOutput[] = new int[] { 18, 46, 36 };
		int customDemand[] = new int[] { 16, 17, 14, 16, 5, 7, 18, 7};
		int customDistances[][] = new int[][] {
			{1411, 	566, 	466, 	567,	1796,	1669,	38,		1463},
			{916,	200,	133,	126,	1209,	1171,	503,	965},
			{783,	70,		168,	49,		1169,	1041,	679,	835}
		};
		
		System.out.println("Välkommen till Bobs KilometerLastbils-räknare.\n");
		
		optimizers = new IOptimizer[] {
			
			// fast and simple distributors
			new OptimizerEvenDistribution(),
			new OptimizerShortestDistance(),
			//new OptimizerBiggestDemand(),
			new OptimizerBiggestDifference(),
			
			// sloppy "equilibrium optimization" on the base distributions
			// these are fairly slow on factory > 2 and store > 7
//			new OptimizerCleverGirl(new OptimizerShortestDistance(), false),
//			new OptimizerCleverGirl(new OptimizerBiggestDemand(), false),
//			new OptimizerCleverGirl(new OptimizerBiggestDifference(), false),
//			
//			// thorough "equilibrium optimization" on the base distributions
//			// these are very slow on factory > 2
//			new OptimizerCleverGirl(new OptimizerShortestDistance(), true),
//			new OptimizerCleverGirl(new OptimizerBiggestDemand(), true),
//			new OptimizerCleverGirl(new OptimizerBiggestDifference(), true),
//			
//			// stepping stone optimization on the base distributions
//			new OptimizerSteppingStone(new OptimizerShortestDistance()),
//			new OptimizerSteppingStone(new OptimizerBiggestDemand()),
//			new OptimizerSteppingStone(new OptimizerBiggestDifference()),
//			
			// stepping stone optimization on sloppy equimized base distributions
			//new OptimizerSteppingStone(new OptimizerCleverGirl(new OptimizerShortestDistance(), false)),
			//new OptimizerSteppingStone(new OptimizerCleverGirl(new OptimizerBiggestDemand(), false)),
			//new OptimizerSteppingStone(new OptimizerCleverGirl(new OptimizerBiggestDifference(), false)),

			// benchmark against this shitter
			new OptimizerEvenDistribution()
		};
		
		scores = new double[optimizers.length-1][ROUNDS];
		timings = new long[optimizers.length-1][ROUNDS];
		
		if(CUSTOM) {
			System.out.println("Running custom model with " + customFactories + " factories and " + customStores + " stores."); 
		}
		else {
			System.out.println("Running " + ROUNDS + " models starting at seed " + STARTING_SEED + " with " + FACTORY_COUNT + " factories and " + STORE_COUNT + " stores.");
		}
		
		for(int round = 0; round < ROUNDS; round++) {
			
			// sanity-"debug"
//			if(FACTORY_COUNT > 2 && !CUSTOM && !VERBOSE) {
//				System.out.print(".");
//				if((round + 1) % 10 == 0) {
//					System.out.print("|");
//				}
//				if((round + 1) % 100 == 0) {
//					System.out.println((round + 1) + "/" + ROUNDS);
//				}
//			}
			
			int totalImpact = 0;
			int impact[] = new int[optimizers.length];
			if(VERBOSE) {
				System.out.println();
			}
			
			int optimizerIndex = 0;
			for(IOptimizer optimizer : optimizers) {

				Model model;
				if(CUSTOM) {
					model = new Model(customFactories, customStores, customOutput, customDemand, customDistances);
				}
				else{
					model = new Model(STARTING_SEED + round, FACTORY_COUNT, STORE_COUNT);
				}
				
				long startingTime = System.nanoTime();
				try {
					optimizer.run(model, VERBOSE);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(optimizerIndex < optimizers.length - 1) {
					timings[optimizerIndex][round] = System.nanoTime() - startingTime;
				}
				
				
				boolean verify = model.verify(false);
				if(VERBOSE || !verify || CUSTOM) {
					System.out.println(optimizer.getName() + " running on model with seed " + STARTING_SEED + " " + 
							(verify ? "PASSED!" : "FAILED!"));
				}
				if(!verify || VERBOSE) {
					model.verify(true);
				}
				impact[optimizerIndex] = verify ? model.getKilometerLastbils() : 0;
				totalImpact += impact[optimizerIndex];
				if(VERBOSE || CUSTOM) {
					System.out.println("Final environmental impact was " + impact[optimizerIndex]);
					model.printMe();
				}
				optimizerIndex++;
			}
			
			for(int optimizer = 0; optimizer < optimizers.length - 1; optimizer++) {
				scores[optimizer][round] = impact[optimizer] == 0 ? 0 : (1.0 * impact[optimizers.length - 1]) / (1.0 * impact[optimizer]);
			}
			
			// NOTHING BUT PRINTING AND SCORING BELOW
			if((round + 1) % PRINT_INTERVAL == 0 || round >= ROUNDS - 1 || CUSTOM) {

				System.out.println();
				
				// make sexy score board, lising optimizer placing totals
				int placings[][] = new int[optimizers.length-1][optimizers.length-1]; // [optimizer][# of placing #X]
				int soloPlacings[][] = new int[optimizers.length-1][optimizers.length-1]; // [optimizer][# of placing #X]
				for(int scoreRound = 0; scoreRound <= round; scoreRound++) {
					
					// get score for this round
					double scoreboard[][] = new double[optimizers.length-1][2]; // [id][optimizer, score]
					for(int optimizer = 0; optimizer < optimizers.length-1; optimizer++) {
						scoreboard[optimizer] = new double[] { optimizer, scores[optimizer][scoreRound] };
					}
					
					// sort descending
					for(int j = 0; j < optimizers.length - 2; j++) {
						for(int k = j; k < optimizers.length - 1; k++) {
							if(scoreboard[j][1] < scoreboard[k][1]) {
								double temp[] = scoreboard[k];
								scoreboard[k] = scoreboard[j];
								scoreboard[j] = temp;
							}
						}
					}
					
					// save placings
					int solo[] = new int[optimizers.length];
					int tempPlacing[] = new int[optimizers.length];
					for(int placing = 0; placing < optimizers.length - 1; placing++) {
						int placingAdjusted = placing;
						for(int tieOffset = 1; tieOffset <= placing; tieOffset++) {
							if(SCORE_IS_TIE > Math.abs(scoreboard[placing][1] - scoreboard[placing - tieOffset][1])) {
								placingAdjusted--;
							}
							else {
								break;
							}
						}
						if(placingAdjusted != placing) {
							placings[(int) scoreboard[placing][0]][placingAdjusted] += 1;
						}
						else {
							placings[(int) scoreboard[placing][0]][placing] += 1;
						}
						solo[placingAdjusted]++;
						tempPlacing[placingAdjusted] = (int) scoreboard[placing][0];
					}
					for(int i = 0; i<solo.length;i++) {
						if(solo[i] == 1) {
							soloPlacings[tempPlacing[i]][i]++;
						}
					}
				}
				
				// scoring and prints down below
				
				// sexy formatting
				String tableFormat = "%-65s" + new String(new char[optimizers.length - 1]).replace("\0", "%20s") + "\n";
				
				// table header for sexy score board
				Object[] row = new String[optimizers.length];
				row[0] = "";
				for(int col = 1; col < optimizers.length; col++) {
					row[col] = "#" + col;
				}
				
				// print sexy header
				System.out.format(tableFormat, row);
				
				// # of placings at 1st, 2nd etc in sexy score board
				for(int optimizer = 0; optimizer < optimizers.length - 1; optimizer++) {
					row[0] = optimizers[optimizer].getName();
					for(int placing = 0; placing < optimizers.length - 1; placing++) {
						row[placing + 1] = "" + placings[optimizer][placing] + "(" + soloPlacings[optimizer][placing] + ")";
					}
					
					// print sexy content
					System.out.format(tableFormat, row);
				}
				if(round >= ROUNDS - 1 && !CUSTOM) {
					// best and average
					double best[] = new double[optimizers.length - 1];
					double tally[] = new double[optimizers.length - 1];
					for(int optimizer = 0; optimizer < optimizers.length - 1; optimizer++) {
						tally[optimizer] = 0;
						for(int scoreRound = 0; scoreRound < ROUNDS; scoreRound++) {
							
							// add up for total
							tally[optimizer] += scores[optimizer][scoreRound];
							
							// find best
							if(scores[optimizer][scoreRound] > best[optimizer]) {
								best[optimizer] = scores[optimizer][scoreRound];
							}
						}
						
						// average out total
						tally[optimizer] /= 1.0 * ROUNDS;
					}
					
					// print avg
					System.out.println();
					for(int i = 0; i < tally.length; i++) {
						System.out.println(optimizers[i].getName() + " got an average score of " + tally[i]);
					}
					
					// print best
					System.out.println();
					for(int i = 0; i < best.length; i++) {
						System.out.println(optimizers[i].getName() + " got a personal best of " + best[i]);
					}
					
					// print average run-time
					System.out.println();
					for(int optimizer = 0; optimizer < optimizers.length - 1; optimizer++) {
						long total = 0;
						for(int scoreRound = 0; scoreRound < ROUNDS; scoreRound++) {
							total += timings[optimizer][scoreRound];
						}
						double average = (1.0 * total) / (1.0 * ROUNDS);
						average /= 1000000; // ns -> ms
						System.out.println(optimizers[optimizer].getName() + " got an average execution time of " + String.format(Locale.US, "%.3f", average) + " milliseconds.");
					}
				}
			}
			if(CUSTOM) {
				break;
			}
		}
		
	}
}
