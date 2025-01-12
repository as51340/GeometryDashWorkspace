package hr.fer.zemris.project.geometry.dash.ai.genetic_programming;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import hr.fer.zemris.project.geometry.dash.ai.AIConstants;
import hr.fer.zemris.project.geometry.dash.ai.AIGameSceneListenerImpl;
import hr.fer.zemris.project.geometry.dash.model.GameEngine;
import hr.fer.zemris.project.geometry.dash.model.PlayingMode;
import hr.fer.zemris.project.geometry.dash.model.drawables.environment.Obstacle;
import hr.fer.zemris.project.geometry.dash.model.drawables.player.Player;
import hr.fer.zemris.project.geometry.dash.model.listeners.AIGameSceneListener;
import hr.fer.zemris.project.geometry.dash.model.math.Vector2D;
import hr.fer.zemris.project.geometry.dash.model.settings.GameConstants;
import hr.fer.zemris.project.geometry.dash.visualization.GameSceneController;
import javafx.application.Platform;

/**
 * Implementation of customable genetic algorithm
 * 
 * @author Andi Škrgat
 *
 */
public class GeneticFunctionality {

	/**
	 * Random reference
	 */
	private Random random;

	/**
	 * By default we use three tournament selection
	 */
	private SelectionProcess selProcess = SelectionProcess.THREE_TOURNAMENT_SELECTION;

	/**
	 * By default we use eliminative genetic algorithm
	 */
	private GenAlg genAlg = GenAlg.ELIMINATIVE_GENETIC_ALGORITHM;

	/**
	 * Current inputs for genetic algorithm
	 */
	private List<Double> inputs = null;

	/**
	 * Sum of all goodness values in population
	 */
	private double population_goodness_value = -1;

	/**
	 * Worst in population
	 */
	private double worst_goodness_value_in_population = Double.MAX_VALUE;

	/**
	 * Locking object
	 */
	private Object lockingObject;

	/**
	 * Locking on continue object
	 */
	private Object continueLockingObject;

	/**
	 * elitistic algorithm
	 */
	private Entry<Player, Tree> bestOfAll = null;

	/**
	 * Best in generation
	 */
	private Entry<Player, Tree> bestInGeneration = null;

	/**
	 * Initial depth of tree
	 */
	private int initialDepth = 6;

	/**
	 * Maximum depth of tree
	 */
	private int maxTreeDepth = 17;

	/**
	 * Should be okay
	 */
	private int treePopulationSize;

	/**
	 * Population of trees
	 */
	private Map<Player, Tree> population;

	/**
	 * Game scene controller
	 */
	private GameSceneController controller;

	/**
	 * Game scene controller
	 */
	private AIGameSceneListener gameSceneListener;

	/**
	 * Is pause pressed
	 */
	private boolean pausePressed = false;

	/**
	 * @return the pausePressed
	 */
	public boolean isPausePressed() {
		return pausePressed;
	}

	/**
	 * @param pausePressed the pausePressed to set
	 */
	public void setPausePressed(boolean pausePressed) {
		this.pausePressed = pausePressed;
	}

	/**
	 * We have
	 * <code>initialDepth<code> levels(2 to <code>initialDepth<code>) and <code>treePopulationSize</code>
	 * divided by <code>initialDepth</code> is populationSizeOfEachDepth
	 */
	private int populationSizeOfEachDepth;

	/**
	 * We have same number of trees generated by full and grow method
	 */
	private int populationSizeFullGrow;
	

	/**
	 * @return the controller
	 */
	public GameSceneController getController() {
		return controller;
	}

	/**
	 * @param controller the controller to set
	 */
	public void setController(GameSceneController controller) {
		this.controller = controller;
	}

	/**
	 * @return the continueLockingObject
	 */
	public Object getContinueLockingObject() {
		return continueLockingObject;
	}

	/**
	 * @param continueLockingObject the continueLockingObject to set
	 */
	public void setContinueLockingObject(Object continueLockingObject) {
		this.continueLockingObject = continueLockingObject;
	}

	/**
	 * Default constructor
	 */
	public GeneticFunctionality() {
		random = new Random();
		population = new HashMap<Player, Tree>();
		inputs = new ArrayList<Double>();
		for(int i = 0; i < 31; i++) {
			inputs.add(0.0);
		}
		gameSceneListener = new AIGameSceneListenerImpl();
	}

	public GeneticFunctionality(int treePopulationSize, String selectionProcess, String geneticAlgorithm) {
		this();
		this.treePopulationSize = treePopulationSize;
		this.populationSizeOfEachDepth = treePopulationSize / (initialDepth - 1);
		populationSizeFullGrow = populationSizeOfEachDepth / 2;
		if (selectionProcess.equals("THREE_TOURNAMENT_SELECTION")) {
			this.selProcess = SelectionProcess.THREE_TOURNAMENT_SELECTION;
		} else if (selectionProcess.equals("ROULETTE_WHEEL_SELECTION")) {
			this.selProcess = SelectionProcess.ROULETTE_WHEEL_SELECTION;
		} else {
			throw new IllegalArgumentException("Unknown selection process!");
		}
		if (geneticAlgorithm.equals("ELIMINATIVE_GENETIC_ALGORITHM")) {
			this.genAlg = GenAlg.ELIMINATIVE_GENETIC_ALGORITHM;
		} else if (geneticAlgorithm.equals("GENERATIONAL_GENETIC_ALGORITHM")) {
			this.genAlg = GenAlg.GENERATIONAL_GENETIC_ALGORITHM;
		} else {
			throw new IllegalArgumentException("Unknown genetic algorithm!");
		}
	}

	/**
	 * @return the gameSceneListener
	 */
	public AIGameSceneListener getGameSceneListener() {
		return gameSceneListener;
	}

	/**
	 * @param gameSceneListener the gameSceneListener to set
	 */
	public void setGameSceneListener(AIGameSceneListener gameSceneListener) {
		this.gameSceneListener = gameSceneListener;
	}

	/**
	 * @return the bestInGeneration
	 */
	public Entry<Player, Tree> getBestInGeneration() {
		return bestInGeneration;
	}

	/**
	 * @param bestInGeneration the bestInGeneration to set
	 */
	public void setBestInGeneration(Entry<Player, Tree> bestInGeneration) {
		this.bestInGeneration = bestInGeneration;
	}

	/**
	 * @return the lockingObject
	 */
	public Object getLockingObject() {
		return lockingObject;
	}

	/**
	 * @param lockingObject the lockingObject to set
	 */
	public void setLockingObject(Object lockingObject) {
		this.lockingObject = lockingObject;
	}

	/**
	 * @return the bestOfAll
	 */
	public Entry<Player, Tree> getBestOfAll() {
		return bestOfAll;
	}

	/**
	 * @param bestOfAll the bestOfAll to set
	 */
	public void setBestOfAll(Entry<Player, Tree> bestOfAll) {
		this.bestOfAll = bestOfAll;
	}

	/**
	 * @return the population
	 */
	public Map<Player, Tree> getPopulation() {
		return population;
	}

	/**
	 * @param population the population to set
	 */
	public void setPopulation(Map<Player, Tree> population) {
		this.population = population;
	}

	/**
	 * @return the population_goodness_value
	 */
	public double getPopulation_goodness_value() {
		return population_goodness_value;
	}

	/**
	 * @param population_goodness_value the population_goodness_value to set
	 */
	public void setPopulation_goodness_value(double population_goodness_value) {
		this.population_goodness_value = population_goodness_value;
	}

	/**
	 * @return the worst_goodness_value_in_population
	 */
	public double getWorst_goodness_value_in_population() {
		return worst_goodness_value_in_population;
	}

	/**
	 * @param worst_goodness_value_in_population the
	 *                                           worst_goodness_value_in_population
	 *                                           to set
	 */
	public void setWorst_goodness_value_in_population(double worst_goodness_value_in_population) {
		this.worst_goodness_value_in_population = worst_goodness_value_in_population;
	}

	/**
	 * @return the initialDepth
	 */
	public int getInitialDepth() {
		return initialDepth;
	}

	/**
	 * @param initialDepth the initialDepth to set
	 */
	public void setInitialDepth(int initialDepth) {
		this.initialDepth = initialDepth;
	}

	/**
	 * @return the maxTreeDepth
	 */
	public int getMaxTreeDepth() {
		return maxTreeDepth;
	}

	/**
	 * @param maxTreeDepth the maxTreeDepth to set
	 */
	public void setMaxTreeDepth(int maxTreeDepth) {
		this.maxTreeDepth = maxTreeDepth;
	}

	/**
	 * @return the treePopulationSize
	 */
	public int getTreePopulationSize() {
		return treePopulationSize;
	}

	/**
	 * @param treePopulationSize the treePopulationSize to set
	 */
	public void setTreePopulationSize(int treePopulationSize) {
		this.treePopulationSize = treePopulationSize;
	}

	/**
	 * @return the populationSizeOfEachDepth
	 */
	public int getPopulationSizeOfEachDepth() {
		return populationSizeOfEachDepth;
	}

	/**
	 * @param populationSizeOfEachDepth the populationSizeOfEachDepth to set
	 */
	public void setPopulationSizeOfEachDepth(int populationSizeOfEachDepth) {
		this.populationSizeOfEachDepth = populationSizeOfEachDepth;
	}

	/**
	 * @return the populationSizeFullGrow
	 */
	public int getPopulationSizeFullGrow() {
		return populationSizeFullGrow;
	}

	/**
	 * @param populationSizeFullGrow the populationSizeFullGrow to set
	 */
	public void setPopulationSizeFullGrow(int populationSizeFullGrow) {
		this.populationSizeFullGrow = populationSizeFullGrow;
	}

	/**
	 * Genetic algorithm
	 */
	public void performAlgorithm() {
		loop();
	}

	/**
	 * We initialize population of size {@linkplain AIConstants}.treePopulationSize
	 * using RHH method. With this method we create same number of trees for every
	 * tree size between 2 and {@linkplain AIConstants}.initialDepth. Half of these
	 * trees are generated with full method and half with grow method
	 */
	public void initializePopulation() {
		for (int i = 2; i <= initialDepth; i++) { // generate for each level
			for (int j = 1; j <= 2; j++) { // first generate trees by full method, then by grow method
				for (int k = 1; k <= populationSizeFullGrow; k++) { // half goes for grow, half goes for
					TreeNode root = new TreeNode(new Action(TreeUtil.numberToActionType(random.nextInt(25) + 1)));
					if (j == 1) { // generate full tree
						TreeUtil.randomSubtree(root, i - 1, random, inputs, true);
					} else { // if tree is generated with grow method
						TreeUtil.randomSubtree(root, i - 1, random, inputs, false);
					}
					Tree tree = new Tree(root);
					Player player = new Player(
							new Vector2D(0, GameConstants.floorPosition_Y - GameConstants.iconHeight - 5),
							new Vector2D(GameConstants.playerSpeed_X, GameConstants.playerSpeed_Y),
							PlayingMode.NEURAL_NETWORK);
					population.put(player, tree);
					if (!TreeUtil.checkIfValid(tree.getRoot())) {
						TreeUtil.printTree(tree.getRoot(), 0);
						System.exit(-1);
					}
				}
			}
		}
	}

	/**
	 * Create algorithm
	 */
	@SuppressWarnings("deprecation")
	private void loop() {
		if (genAlg == GenAlg.ELIMINATIVE_GENETIC_ALGORITHM) {
			for (int i = 0; i < AIConstants.maxGenerations; i++) {

				Platform.runLater(() -> {
					controller.updateLabel();
				});

				GameEngine.getInstance().getGameStateListener().AITrainingModePlayingStarted();
				GameEngine.getInstance().getGameWorld().setUnlockingCondition(false);
				GameEngine.getInstance().getGameWorld().setLevelPassed(false);

				population_goodness_value = 0;
				worst_goodness_value_in_population = Double.MAX_VALUE;
				bestInGeneration = null;

				Entry<Player, Tree> minPlayerTree = null;
				Entry<Player, Tree> almostMinPlayerTree = null;
				Entry<Player, Tree> maxPlayerTree = null;

				synchronized (lockingObject) {
					while (!GameEngine.getInstance().getGameWorld().isUnlockingCondition()) {
						try {
							lockingObject.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

				for (Entry<Player, Tree> entry : population.entrySet()) {
					double value = entry.getKey().getGoodness_value();
					population_goodness_value += value;
					if (minPlayerTree == null || value < worst_goodness_value_in_population) {
						worst_goodness_value_in_population = value;
						almostMinPlayerTree = minPlayerTree;
						minPlayerTree = entry;
					} else if (almostMinPlayerTree == null
							|| value < almostMinPlayerTree.getKey().getGoodness_value()) {
						almostMinPlayerTree = entry;
					}
					if (maxPlayerTree == null || value > maxPlayerTree.getKey().getGoodness_value()) {
						maxPlayerTree = entry;
					}
				}

				population.remove(minPlayerTree.getKey());
				population.remove(almostMinPlayerTree.getKey());

				//System.out.println(GameEngine.getInstance().getGameWorld().getGpAlgorithm().getPopulation().size());
				
				if (bestOfAll == null
						|| maxPlayerTree.getKey().getGoodness_value() > bestOfAll.getKey().getGoodness_value()) {
					bestOfAll = maxPlayerTree;
				}
				bestInGeneration = maxPlayerTree;

				if (GameEngine.getInstance().getGameWorld().isLevelPassed()) {

					// probably on javafx application thread
					Platform.runLater(() -> {
						controller.interruptTraining(PlayingMode.GENETIC_PROGRAMMING, bestOfAll.getValue(),
								GameEngine.getInstance().getGameWorld().isLevelPassed()); // handlaj
																							// fail
					});
					if (continueLockingObject != null) {
						synchronized (continueLockingObject) { // only one thread so we shouldn't need while loop
							try {
								continueLockingObject.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					} else {
						Thread.currentThread().stop();
					}
				}
				if (pausePressed) {
					synchronized (continueLockingObject) { // only one thread so we shouldn't need while loop
						try {
							continueLockingObject.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

				List<Tree> selectedTrees = selection();
				List<Tree> crossovered = null;
				boolean valid = false;
				while (!valid) {
					int firstTree = random.nextInt(selectedTrees.get(0).getSize()) + 1;
					int secondTree = random.nextInt(selectedTrees.get(1).getSize()) + 1;
					crossovered = TreeUtil.crossover(selectedTrees.get(0), selectedTrees.get(1), firstTree, secondTree);
					valid = TreeUtil.checkIfValid(crossovered.get(0).getRoot()) && TreeUtil.checkIfValid(crossovered.get(1).getRoot());
				}

				valid = false;
				Tree mutated1 = null;
				while (!valid) {
					int targetNode1 = random.nextInt(crossovered.get(0).getSize()) + 1;
					try {
						mutated1 = TreeUtil.mutate(crossovered.get(0), targetNode1, random, inputs);
						valid = TreeUtil.checkIfValid(mutated1.getRoot());
					} catch (IllegalArgumentException e) {
						valid = false;
					}
				}

				valid = false;
				Tree mutated2 = null;
				while (!valid) {
					int targetNode2 = random.nextInt(crossovered.get(0).getSize()) + 1;
					try {
						mutated2 = TreeUtil.mutate(crossovered.get(0), targetNode2, random, inputs);
						valid = TreeUtil.checkIfValid(mutated2.getRoot());
					} catch (IllegalArgumentException e) {
						valid = false;
					}
				}

				Player p1 = new Player(new Vector2D(0, GameConstants.floorPosition_Y - GameConstants.iconHeight - 5),
						new Vector2D(GameConstants.playerSpeed_X, GameConstants.playerSpeed_Y),
						PlayingMode.GENETIC_PROGRAMMING);
				Player p2 = new Player(new Vector2D(0, GameConstants.floorPosition_Y - GameConstants.iconHeight - 5),
						new Vector2D(GameConstants.playerSpeed_X, GameConstants.playerSpeed_Y),
						PlayingMode.GENETIC_PROGRAMMING);

				population.put(p1, mutated1);
				population.put(p2, mutated2);

//				System.out.println("Population size after adding " + population.size());
//				GameEngine.getInstance().getGameWorld().addPlayer(p1);
//				GameEngine.getInstance().getGameWorld().addPlayer(p2);
//				GameEngine.getInstance().getGameWorld().getRenderer().addGameObject(p1);
//				GameEngine.getInstance().getGameWorld().getRenderer().addGameObject(p2);
				GameEngine.getInstance().getGameWorld().setGpAlgorithm(this);
				GameEngine.getInstance().getGameWorld().createAIScene();
			}
			Platform.runLater(() -> {
				controller.interruptTraining(PlayingMode.GENETIC_PROGRAMMING, bestOfAll.getValue(),
						true); // handlaj	
			});
			
																				// fail
		} else if (genAlg == GenAlg.GENERATIONAL_GENETIC_ALGORITHM) {
			int genNumber = 0;
			while (genNumber < AIConstants.maxGenerations) {
				Platform.runLater(() -> {
					controller.updateLabel();
				});

				GameEngine.getInstance().getGameStateListener().AITrainingModePlayingStarted();
				GameEngine.getInstance().getGameWorld().setUnlockingCondition(false);
				GameEngine.getInstance().getGameWorld().setLevelPassed(false);

				worst_goodness_value_in_population = Double.MAX_VALUE;

				Entry<Player, Tree> bestPlayerTree = null;
				Entry<Player, Tree> almostBestPlayerTree = null;

				synchronized (lockingObject) {
					while (!GameEngine.getInstance().getGameWorld().isUnlockingCondition()) {
						try {
							lockingObject.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}			

				for (Entry<Player, Tree> entry : population.entrySet()) {
					double value = entry.getKey().getGoodness_value();
					population_goodness_value += value;
					if (value < worst_goodness_value_in_population) {
						worst_goodness_value_in_population = value;
					}
					if (bestPlayerTree == null || value > bestPlayerTree.getKey().getGoodness_value()) {
						almostBestPlayerTree = bestPlayerTree;
						bestPlayerTree = entry;
					} else if (almostBestPlayerTree == null
							|| value > almostBestPlayerTree.getKey().getGoodness_value()) {
						almostBestPlayerTree = entry;
					}
				}

				Player p1 = new Player(new Vector2D(0, GameConstants.floorPosition_Y - GameConstants.iconHeight - 5),
						new Vector2D(GameConstants.playerSpeed_X, GameConstants.playerSpeed_Y),
						PlayingMode.GENETIC_PROGRAMMING);
				Player p2 = new Player(new Vector2D(0, GameConstants.floorPosition_Y - GameConstants.iconHeight - 5),
						new Vector2D(GameConstants.playerSpeed_X, GameConstants.playerSpeed_Y),
						PlayingMode.GENETIC_PROGRAMMING);

				Map<Player, Tree> newPopulation = new HashMap<Player, Tree>();
				newPopulation.put(p1, bestPlayerTree.getValue());
				newPopulation.put(p2, almostBestPlayerTree.getValue());

				// dodaj 2 najbolja

				if (bestOfAll == null
						|| bestPlayerTree.getKey().getGoodness_value() > bestOfAll.getKey().getGoodness_value()) {
					bestOfAll = bestPlayerTree;
				}

				bestInGeneration = bestPlayerTree;

				if (GameEngine.getInstance().getGameWorld().isLevelPassed()) {
					// probably on javafx application thread
					Platform.runLater(() -> {
						controller.interruptTraining(PlayingMode.GENETIC_PROGRAMMING, bestOfAll.getValue(),
								GameEngine.getInstance().getGameWorld().isLevelPassed()); // handlaj
																							// fail
					});
					if (continueLockingObject != null) {
						synchronized (continueLockingObject) { // only one thread so we shouldn't need while loop
							try {
								continueLockingObject.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					} else {
						Thread.currentThread().stop();
					}
				}
				if (pausePressed) {
					synchronized (continueLockingObject) { // only one thread so we shouldn't need while loop
						try {
							continueLockingObject.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				while (newPopulation.size() < treePopulationSize) {
					List<Tree> selectedTrees = selection();
					List<Tree> crossovered = null;
					boolean valid = false;
					while (!valid) {
						int firstTree = random.nextInt(selectedTrees.get(0).getSize()) + 1;
						int secondTree = random.nextInt(selectedTrees.get(1).getSize()) + 1;
						crossovered = TreeUtil.crossover(selectedTrees.get(0), selectedTrees.get(1), firstTree,
								secondTree);
						valid = true;
						valid = TreeUtil.checkIfValid(crossovered.get(0).getRoot()) && TreeUtil.checkIfValid(crossovered.get(1).getRoot());
					}

					valid = false;
					Tree mutated1 = null;
					while (!valid) {
						int targetNode1 = random.nextInt(crossovered.get(0).getSize()) + 1;
						try {
							mutated1 = TreeUtil.mutate(crossovered.get(0), targetNode1, random, inputs);
							valid = TreeUtil.checkIfValid(mutated1.getRoot());
						} catch (IllegalArgumentException e) {
							valid = false;
						}
					}

					valid = false;
					Tree mutated2 = null;
					while (!valid) {
						int targetNode2 = random.nextInt(crossovered.get(0).getSize()) + 1;
						try {
							mutated2 = TreeUtil.mutate(crossovered.get(0), targetNode2, random, inputs);
							valid = TreeUtil.checkIfValid(mutated2.getRoot());
						} catch (IllegalArgumentException e) {
							valid = false;
						}
					}

					Player p3 = new Player(
							new Vector2D(0, GameConstants.floorPosition_Y - GameConstants.iconHeight - 5),
							new Vector2D(GameConstants.playerSpeed_X, GameConstants.playerSpeed_Y),
							PlayingMode.GENETIC_PROGRAMMING);
					Player p4 = new Player(
							new Vector2D(0, GameConstants.floorPosition_Y - GameConstants.iconHeight - 5),
							new Vector2D(GameConstants.playerSpeed_X, GameConstants.playerSpeed_Y),
							PlayingMode.GENETIC_PROGRAMMING);

					newPopulation.put(p3, mutated1);
					newPopulation.put(p4, mutated2);
				}
				GameEngine.getInstance().getGameWorld().setGpAlgorithm(this); // not sure if needs that
				GameEngine.getInstance().getGameWorld().createAIScene();
				genNumber++;

			}
			Platform.runLater(() -> {
				controller.interruptTraining(PlayingMode.GENETIC_PROGRAMMING, bestOfAll.getValue(),
						true); // handlaj	
			});
		}
	}

	/**
	 * Selection algorithm
	 * 
	 * @return list of selected trees
	 */
	private List<Tree> selection() {
		List<Tree> list = new ArrayList<Tree>();
		Object[] values = population.values().toArray();
		Object[] keys = population.keySet().toArray();
		if (selProcess == SelectionProcess.THREE_TOURNAMENT_SELECTION) {
			int[] indexes = random.ints(0, treePopulationSize - 2).distinct().limit(3).toArray();

			Player p1, p2, p3;
			p1 = (Player) keys[indexes[0]];
			p2 = (Player) keys[indexes[1]];
			p3 = (Player) keys[indexes[2]];
			list.add((Tree) values[indexes[0]]);
			list.add((Tree) values[indexes[1]]);
			list.add((Tree) values[indexes[2]]);

			if (p1.getGoodness_value() < p2.getGoodness_value() && p1.getGoodness_value() < p3.getGoodness_value()) {
				list.remove(0);
			} else if (p2.getGoodness_value() < p1.getGoodness_value()
					&& p2.getGoodness_value() < p3.getGoodness_value()) {
				list.remove(1);
			} else {
				list.remove(2);
			}
		} else if (selProcess == SelectionProcess.ROULETTE_WHEEL_SELECTION) {
			Tree tree1 = getPlayerByRoulette();
			if (tree1 == null) { // uniformly select
				tree1 = (Tree) values[random.nextInt(treePopulationSize - 2)];
			}
			Tree tree2 = getPlayerByRoulette();
			if (tree2 == null) {
				tree2 = (Tree) values[random.nextInt(treePopulationSize - 2)];
			}
			list.add(tree1);
			list.add(tree2);
		}
		return list;
	}

	/**
	 * @return tree using roulette algorithm in which better individual has more
	 *         chances to be selected
	 * 
	 */
	private Tree getPlayerByRoulette() {
//		System.out.println("Population " + population_goodness_value);
//		System.out.println("Worst in population" + worst_goodness_value_in_population);
		Object[] values = population.values().toArray();
		Object[] keys = population.keySet().toArray();
		double d = random.nextDouble()
				* (population_goodness_value - worst_goodness_value_in_population * (treePopulationSize - 2));
		if (d < 0 && Math.abs(d) > 1e-3) {
			//System.out.println("d " + d);
			throw new IllegalArgumentException("Something is wrong with roulette_wheel selection");
		}
		double curr = 0;
		for (int i = 0; i < treePopulationSize - 2; i++) { // jer smo 2 removali
			if (curr + (((Player) keys[i]).getGoodness_value() - worst_goodness_value_in_population)
					/ population_goodness_value > d) {
				return (Tree) values[i];
			}
			curr += (((Player) keys[i]).getGoodness_value() - worst_goodness_value_in_population);
		}
		return null;
	}

	/**
	 * @return the random
	 */
	public Random getRandom() {
		return random;
	}

	/**
	 * @param random the random to set
	 */
	public void setRandom(Random random) {
		this.random = random;
	}

	/**
	 * @return the inputs
	 */
	public List<Double> getInputs() {
		return inputs;
	}

	/**
	 * @param inputs the inputs to set
	 */
	public void setInputs(List<Double> inputs) {
		this.inputs = inputs;
	}

	/**
	 * @return the selProcess
	 */
	public SelectionProcess getSelProcess() {
		return selProcess;
	}

	/**
	 * @param selProcess the selProcess to set
	 */
	public void setSelProcess(SelectionProcess selProcess) {
		this.selProcess = selProcess;
	}

	/**
	 * @return the genAlg
	 */
	public GenAlg getGenAlg() {
		return genAlg;
	}

	/**
	 * @param genAlg the genAlg to set
	 */
	public void setGenAlg(GenAlg genAlg) {
		this.genAlg = genAlg;
	}

}
