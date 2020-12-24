package hr.fer.zemris.project.geometry.dash.ai.geneticNeuralNetwok;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

import hr.fer.zemris.project.geometry.dash.ai.AIConstants;
import hr.fer.zemris.project.geometry.dash.ai.ElmanNeuralNetwork;
import hr.fer.zemris.project.geometry.dash.ai.GeneticNeuralNetwork;
import hr.fer.zemris.project.geometry.dash.ai.NeuralNetwork;
import hr.fer.zemris.project.geometry.dash.model.GameEngine;
import hr.fer.zemris.project.geometry.dash.model.PlayingMode;
import hr.fer.zemris.project.geometry.dash.model.drawables.player.Player;
import hr.fer.zemris.project.geometry.dash.model.math.Vector2D;
import hr.fer.zemris.project.geometry.dash.model.settings.GameConstants;

public class AIAlgorithm {

    private static final int POPULATION_SIZE = 300;
    private static final int REPEAT = 500;
    private static final double MUTATION_RATE = 0.1;
    private static final int INPUT_LAYER_SIZE = AIConstants.numOfClosestObstacles*3 + 1;
    private final int numberOfHiddenLayers;
    private final int numberPerHiddenLayer;
    private DoubleUnaryOperator activationFunction = AIConstants.activationFunction;

    private Map<Player, NeuralNetwork> playerNeuralNetworkMap;
    private int sumOfAllFitnesses;
    /**
     * Which algorithm to run - Genetic or Elman.
     */
    private PlayingMode mode;

    /**
     * If playing mode is set to ELMAN_NEURAL_NETWORK, numberOfHiddenLayers is ignored and silently set to 1
     * 
     * @param numberOfHiddenLayers
     * @param numberPerHiddenLayer
     * @param mode
     */
    public AIAlgorithm(int numberOfHiddenLayers, int numberPerHiddenLayer, PlayingMode mode) {
    	if((mode == PlayingMode.NEURAL_NETWORK || mode == PlayingMode.ELMAN_NEURAL_NETWORK))
    		throw new IllegalArgumentException("Mode has to be NEURAL_NETWORK or ELMAN_NEURAL_NETWORK");
    	if(mode == PlayingMode.ELMAN_NEURAL_NETWORK) {
    		this.numberOfHiddenLayers = 1;
    	} else {
    		this.numberOfHiddenLayers = numberOfHiddenLayers;
    	}
    	
        playerNeuralNetworkMap = new LinkedHashMap<>();
        this.numberPerHiddenLayer = numberPerHiddenLayer;
        this.mode = mode;
    }

    public AIAlgorithm(int numberOfHiddenLayers, int numberPerHiddenLayer, DoubleUnaryOperator activationFunction, PlayingMode mode) {
        this(numberOfHiddenLayers, numberPerHiddenLayer, mode);
        this.activationFunction = activationFunction;
    }

    public void runAlgorithm() {
        initialize();
        for (int i = 0; i < REPEAT; i++) {
            sumOfAllFitnesses = 0;
            selection();
            reproduction();
        }
    }

	private void initialize() {
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Player player = new Player(new Vector2D(i * 20, GameConstants.floorPosition_Y - GameConstants.iconHeight - 5),
                    new Vector2D(GameConstants.playerSpeed_X, GameConstants.playerSpeed_Y), mode);

            NeuralNetwork neuralNetwork = mode == PlayingMode.NEURAL_NETWORK
            		? new GeneticNeuralNetwork(INPUT_LAYER_SIZE, numberOfHiddenLayers, numberPerHiddenLayer, activationFunction)
            		: new ElmanNeuralNetwork(INPUT_LAYER_SIZE, numberPerHiddenLayer, activationFunction);
            
            playerNeuralNetworkMap.put(player, neuralNetwork);
        }
	}

    private void selection() {
        //nekako u gameworld staviti playere, cekati da zavrse igrati da im se napravi fitness
        GameEngine.getInstance().getGameWorld().setPlayers(playerNeuralNetworkMap.keySet());
        //idk kako ovo radi ali cekamo valjda da zavrse igrat??
        //tu bi trebali i ako smo zadovoljni s fitnesom stat? myb idk
        Thread thread = new Thread(() -> {
        	//wait i notify
        });
        //JAVAFX application thread
        
        for (Player player : playerNeuralNetworkMap.keySet())
            sumOfAllFitnesses += player.getGoodness_value();

    }

    private void reproduction() {
        Map<Player, NeuralNetwork> playerNeuralNetworkMap = new HashMap<>();

        for (int i = 0; i < POPULATION_SIZE; i++) {
            Player player = new Player(new Vector2D(i * 20, GameConstants.floorPosition_Y - GameConstants.iconHeight - 5),
                    new Vector2D(GameConstants.playerSpeed_X, GameConstants.playerSpeed_Y), mode);

            NeuralNetwork parent1 = getRandomParent();
            NeuralNetwork parent2 = getRandomParent();

            while (parent2.equals(parent1))
                parent2 = getRandomParent();

            NeuralNetwork child = crossover(parent1, parent2);
            child = mutation(child);
            playerNeuralNetworkMap.put(player, child);
        }

        this.playerNeuralNetworkMap = playerNeuralNetworkMap;
    }

    private NeuralNetwork getRandomParent() {
        int sum = 0;
        int randomSum = (int) Math.ceil(Math.random() * sumOfAllFitnesses);

        NeuralNetwork parent = null;

        for (Map.Entry<Player, NeuralNetwork> entry : playerNeuralNetworkMap.entrySet()) {
            if (sum >= randomSum)
                break;
            sum += entry.getKey().getGoodness_value();
            parent = entry.getValue();
        }

        return parent;
    }

    private NeuralNetwork crossover(NeuralNetwork parent1, NeuralNetwork parent2) {
        List<List<Double>> weights1 = parent1.getWeights(), weights2 = parent2.getWeights();
        List<List<Double>> weightsChild = new ArrayList<>();

        for (int i = 0; i < weights1.size(); i++) {
            List<Double> list1 = weights1.get(i), list2 = weights2.get(i);
            List<Double> child1 = new ArrayList<>();

            for (int j = 0; j < list1.size(); j++) {
                child1.add((list1.get(j) + list2.get(j)) / 2);
            }
            weightsChild.add(child1);
        }

        List<Double> biases1 = parent1.getBiases(), biases2 = parent2.getBiases();
        List<Double> biasesChild = new ArrayList<>();

        for (int i = 0; i < biases1.size(); i++) {
            biasesChild.add((biases1.get(i) + biases2.get(i)) / 2);
        }

        NeuralNetwork childNetwork = mode == PlayingMode.NEURAL_NETWORK
        		? new GeneticNeuralNetwork(INPUT_LAYER_SIZE, numberOfHiddenLayers, numberPerHiddenLayer, activationFunction)
        		: new ElmanNeuralNetwork(INPUT_LAYER_SIZE, numberPerHiddenLayer, activationFunction);
        childNetwork.setWeights(weightsChild);
        childNetwork.setBiases(biasesChild);
        return childNetwork;
    }

    private NeuralNetwork mutation(NeuralNetwork child) {
        List<List<Double>> weights = child.getWeights();
        List<List<Double>> newWeights = new ArrayList<>();
        List<Double> biases = child.getBiases();
        List<Double> newBiases = new ArrayList<>();

        for (List<Double> list : weights) {
            for (int j = 0; j < list.size(); j++) {
                if (shouldIMutate())
                    list.set(j, Math.random() * 2 - 1);
            }
            newWeights.add(list);
        }

        for (Double bias : biases) {
            if (shouldIMutate())
                bias = Math.random() * 2 - 1;
            newBiases.add(bias);
        }

        child.setWeights(newWeights);
        child.setBiases(newBiases);

        return child;
    }

    private boolean shouldIMutate() {
        return Math.random() <= MUTATION_RATE;
    }
}