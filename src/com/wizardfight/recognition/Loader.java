/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wizardfight.recognition;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Konstantin
 */
public class Loader {
    final int NUM_SYMBOLS = 20; // 10 - default
    public KMeansQuantizer quantizer = new KMeansQuantizer(NUM_SYMBOLS);
    public HMM hmm = new HMM();
    
    public boolean loadHMMFromFile(String file) throws IOException {
        hmm.clear();

        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException ex) {
            System.err
                    .println("loadDatasetFromFile(String filename) - FILE NOT OPEN!");
            return false;
        }

        String word;
        double value;

        // Find the file type header
        word = reader.readLine();
        if (!word.contains("HMM_MODEL_FILE_V2.0")) {
            System.err
                    .println("loadModelFromFile( fstream &file ) - Could not find Model File Header!");
            reader.close();
            return false;
        }

        // Load the trained state
        word = reader.readLine();
        if (!word.contains("Trained:")) {
            System.err
                    .println("loadBaseSettingsFromFile(fstream &file) - Failed to read Trained header!");
            reader.close();
            return false;
        }

        String[] buf = word.split(" ");
        hmm.trained = (Integer.parseInt(buf[1]) == 1);

        // Load the scaling state
        word = reader.readLine();
        if (!word.contains("UseScaling:")) {
            System.err
                    .println("loadBaseSettingsFromFile(fstream &file) - Failed to read UseScaling header!");
            reader.close();
            return false;
        }
        buf = word.split(" ");
        hmm.useScaling = (Integer.parseInt(buf[1]) == 1);

        // Load the NumInputDimensions
        word = reader.readLine();
        if (!word.contains("NumInputDimensions:")) {
            System.err
                    .println("loadBaseSettingsFromFile(fstream &file) - Failed to read NumInputDimensions header!");
            reader.close();
            return false;
        }
        buf = word.split(" ");
        hmm.numInputDimensions = Integer.parseInt(buf[1]);

        // Load the NumOutputDimensions
        word = reader.readLine();
        if (!word.contains("NumOutputDimensions:")) {
            System.err
                    .println("loadBaseSettingsFromFile(fstream &file) - Failed to read NumOutputDimensions header!");
            reader.close();
            return false;
        }
		// buf = word.split(" ");
        // numOutputDimensions = Integer.parseInt(buf[1]);

        // Load the numTrainingIterationsToConverge
        word = reader.readLine();
        if (!word.contains("NumTrainingIterationsToConverge:")) {
            System.err
                    .println("loadBaseSettingsFromFile(fstream &file) - Failed to read NumTrainingIterationsToConverge header!");
            reader.close();
            return false;
        }

		// buf = word.split(" ");
        // numTrainingIterationsToConverge = Integer.parseInt(buf[1]);
        // Load the MinNumEpochs
        word = reader.readLine();
        if (!word.contains("MinNumEpochs:")) {
            System.err
                    .println("loadBaseSettingsFromFile(fstream &file) - Failed to read MinNumEpochs header!");
            reader.close();
            return false;
        }

		// buf = word.split(" ");
        // min = Integer.parseInt(buf[1]);
        // Load the maxNumEpochs
        word = reader.readLine();
        if (!word.contains("MaxNumEpochs:")) {
            System.err
                    .println("loadBaseSettingsFromFile(fstream &file) - Failed to read MaxNumEpochs header!");
            reader.close();
            return false;
        }
		// buf = word.split(" "); numStates =
        // Integer.parseInt(buf[1]);maxNumEpochs;

        // Load the ValidationSetSize
        word = reader.readLine();
        if (!word.contains("ValidationSetSize:")) {
            System.err
                    .println("loadBaseSettingsFromFile(fstream &file) - Failed to read ValidationSetSize header!");
            reader.close();
            return false;
        }
		// buf = word.split(" "); numStates =
        // Integer.parseInt(buf[1]);validationSetSize;

        // Load the LearningRate
        word = reader.readLine();
        if (!word.contains("LearningRate:")) {
            System.err
                    .println("loadBaseSettingsFromFile(fstream &file) - Failed to read LearningRate header!");
            reader.close();
            return false;
        }
		// buf = word.split(" "); numStates =
        // Integer.parseInt(buf[1]);learningRate;

        // Load the MinChange
        word = reader.readLine();
        if (!word.contains("MinChange:")) {
            System.err
                    .println("loadBaseSettingsFromFile(fstream &file) - Failed to read MinChange header!");
            reader.close();
            return false;
        }
		// buf = word.split(" "); numStates =
        // Integer.parseInt(buf[1]);minChange;

        // Load the UseValidationSet
        word = reader.readLine();
        if (!word.contains("UseValidationSet:")) {
            System.err
                    .println("loadBaseSettingsFromFile(fstream &file) - Failed to read UseValidationSet header!");
            reader.close();
            return false;
        }
		// buf = word.split(" "); numStates =
        // Integer.parseInt(buf[1]);useValidationSet;

        // Load the RandomiseTrainingOrder
        word = reader.readLine();
        if (!word.contains("RandomiseTrainingOrder:")) {
            System.err
                    .println("loadBaseSettingsFromFile(fstream &file) - Failed to read RandomiseTrainingOrder header!");
            reader.close();
            return false;
        }
		// buf = word.split(" "); numStates =
        // Integer.parseInt(buf[1]);randomiseTrainingOrder;

        // Load if the number of clusters
        word = reader.readLine();
        if (!word.contains("UseNullRejection:")) {
            System.err
                    .println("loadBaseSettingsFromFile(fstream &file) - Failed to read UseNullRejection header!");
            reader.close();
            hmm.clear();
            return false;
        }
        buf = word.split(" ");
        hmm.useNullRejection = (Integer.parseInt(buf[1]) == 1);

        // Load if the classifier mode
        word = reader.readLine();
        if (!word.contains("ClassifierMode:")) {
            System.err
                    .println("loadBaseSettingsFromFile(fstream &file) - Failed to read ClassifierMode header!");
            hmm.clear();
            reader.close();
            return false;
        }

        // Load if the null rejection coeff
        word = reader.readLine();
        if (!word.contains("NullRejectionCoeff:")) {
            System.err
                    .println("loadBaseSettingsFromFile(fstream &file) - Failed to read NullRejectionCoeff header!");
            hmm.clear();
            reader.close();
            return false;
        }

        // If the model is trained then load the model settings
        if (hmm.trained) {
            // Load the number of classes
            word = reader.readLine();
            if (!word.contains("NumClasses:")) {
                System.err
                        .println("loadBaseSettingsFromFile(fstream &file) - Failed to read NumClasses header!");
                hmm.clear();
                reader.close();
                return false;
            }
            buf = word.split(" ");
            hmm.numClasses = Integer.parseInt(buf[1]);

            // Load the null rejection thresholds
            word = reader.readLine();
            if (!word.contains("NullRejectionThresholds:")) {
                System.err
                        .println("loadBaseSettingsFromFile(fstream &file) - Failed to read NullRejectionThresholds header!");
                hmm.clear();
                reader.close();
                return false;
            }
            hmm.nullRejectionThresholds = new double[hmm.numClasses];
            buf = word.split(" ");
            System.out.println(word);
            for (int i = 0; i < hmm.nullRejectionThresholds.length; i++) {

                hmm.nullRejectionThresholds[i] = Integer.parseInt(buf[i + 1]);
            }
            // Load the class labels
            word = reader.readLine();
            if (!word.contains("ClassLabels:")) {
                System.err
                        .println("loadBaseSettingsFromFile(fstream &file) - Failed to read ClassLabels header!");
                hmm.clear();
                reader.close();
                return false;
            }
            hmm.classLabels = new int[hmm.numClasses];
            buf = word.split(" ");
            for (int i = 0; i < hmm.classLabels.length; i++) {
                hmm.classLabels[i] = Integer.parseInt(buf[i + 1]);
            }

            if (hmm.useScaling) {
                // Load if the Ranges
                word = reader.readLine();
                if (!word.contains("Ranges:")) {
                    System.err
                            .println("loadClustererSettingsFromFile(fstream &file) - Failed to read Ranges header!");
                    hmm.clear();
                    reader.close();
                    return false;
                }
				// ranges.resize(numInputDimensions);
                //
                // for (int i = 0; i < ranges.size(); i++) {
                // buf = word.split(" "); numStates =
                // Integer.parseInt(buf[1]);ranges[i].minValue;
                // buf = word.split(" "); numStates =
                // Integer.parseInt(buf[1]);ranges[i].maxValue;
                // }
            }
        }
        word = reader.readLine();
        if (!word.contains("NumStates:")) {
            System.err
                    .println("loadModelFromFile( fstream &file ) - Could not find NumStates.");
            reader.close();
            return false;
        }
        buf = word.split(" ");
        hmm.numStates = Integer.parseInt(buf[1]);

        word = reader.readLine();
        if (!word.contains("NumSymbols:")) {
            System.err
                    .println("loadModelFromFile( fstream &file ) - Could not find NumSymbols.");
            reader.close();
            return false;
        }
        buf = word.split(" ");
        hmm.numSymbols = Integer.parseInt(buf[1]);

        word = reader.readLine();
        if (!word.contains("ModelType:")) {
            System.err
                    .println("loadModelFromFile( fstream &file ) - Could not find ModelType.");
            reader.close();
            return false;
        }
        buf = word.split(" ");
        hmm.modelType = Integer.parseInt(buf[1]) == 0 ? HMMModelTypes.ERGODIC
                : HMMModelTypes.LEFTRIGHT;

        word = reader.readLine();
        if (!word.contains("Delta:")) {
            System.err
                    .println("loadModelFromFile( fstream &file ) - Could not find Delta.");
            reader.close();
            return false;
        }
        buf = word.split(" ");
        hmm.delta = Integer.parseInt(buf[1]);

        word = reader.readLine();
        if (!word.contains("NumRandomTrainingIterations:")) {
            System.err
                    .println("loadModelFromFile( fstream &file ) - Could not find NumRandomTrainingIterations.");
            reader.close();
            return false;
        }
        buf = word.split(" ");
        hmm.numRandomTrainingIterations = Integer.parseInt(buf[1]);

        // If the HMM has been trained then load the hmm.models
        if (hmm.trained) {

            // Resize the buffer
            hmm.models.ensureCapacity(hmm.numClasses);

            // Load each of the K classes
            for (int k = 0; k < hmm.numClasses; k++) {
                int modelID;
                word = reader.readLine();
                if (!word.contains("Model_ID:")) {
                    System.err
                            .println("loadModelFromFile( fstream &file ) - Could not find model ID for the "
                                    + (k + 1) + "th model");
                    reader.close();
                    return false;
                }
                buf = word.split(" ");
                modelID = Integer.parseInt(buf[1]);

                if (modelID - 1 != k) {
                    System.err
                            .println("loadModelFromFile( fstream &file ) - Model ID does not match the current class ID for the "
                                    + (k + 1) + "th model");
                    reader.close();
                    return false;
                }
                word = reader.readLine();
                if (!word.contains("NumStates:")) {
                    System.err
                            .println("loadModelFromFile( fstream &file ) - Could not find the NumStates for the "
                                    + (k + 1) + "th model");
                    reader.close();
                    return false;
                }
                buf = word.split(" ");
                hmm.models.add(k, new HiddenMarkovModel());
                hmm.models.get(k).numStates = Integer.parseInt(buf[1]);

                System.out.println("Num states: " + hmm.numStates);

                word = reader.readLine();
                if (!word.contains("NumSymbols:")) {
                    System.err
                            .println("loadModelFromFile( fstream &file ) - Could not find the NumSymbols for the "
                                    + (k + 1) + "th model");
                    reader.close();
                    return false;
                }
                buf = word.split(" ");
                hmm.models.get(k).numSymbols = Integer.parseInt(buf[1]);

                word = reader.readLine();
                if (!word.contains("ModelType:")) {
                    System.err
                            .println("loadModelFromFile( fstream &file ) - Could not find the modelType for the "
                                    + (k + 1) + "th model");
                    reader.close();
                    return false;
                }
                buf = word.split(" ");
                hmm.models.get(k).modelType = Integer.parseInt(buf[1]) == 0 ? HMMModelTypes.ERGODIC
                        : HMMModelTypes.LEFTRIGHT;

                word = reader.readLine();
                if (!word.contains("Delta:")) {
                    System.err
                            .println("loadModelFromFile( fstream &file ) - Could not find the Delta for the "
                                    + (k + 1) + "th model");
                    reader.close();
                    return false;
                }
                buf = word.split(" ");
                hmm.models.get(k).delta = Integer.parseInt(buf[1]);

                word = reader.readLine();
                if (!word.contains("Threshold:")) {
                    System.err
                            .println("loadModelFromFile( fstream &file ) - Could not find the Threshold for the "
                                    + (k + 1) + "th model");
                    reader.close();
                    return false;
                }
                buf = word.split(" ");
                hmm.models.get(k).cThreshold = Integer.parseInt(buf[1]);

                word = reader.readLine();
                if (!word.contains("NumRandomTrainingIterations:")) {
                    System.err
                            .println("loadModelFromFile( fstream &file ) - Could not find the numRandomTrainingIterations for the "
                                    + (k + 1) + "th model.");
                    reader.close();
                    return false;
                }
                buf = word.split(" ");
                hmm.models.get(k).numRandomTrainingIterations = Integer
                        .parseInt(buf[1]);

                word = reader.readLine();
                if (!word.contains("MaxNumIter:")) {
                    System.err
                            .println("loadModelFromFile( fstream &file ) - Could not find the MaxNumIter for the "
                                    + (k + 1) + "th model.");
                    reader.close();
                    return false;
                }
                buf = word.split(" ");
                hmm.models.get(k).maxNumIter = Integer.parseInt(buf[1]);

                hmm.models.get(k).a.resize(hmm.models.get(k).numStates,
                        hmm.models.get(k).numStates);
                hmm.models.get(k).b.resize(hmm.models.get(k).numStates,
                        hmm.models.get(k).numSymbols);
                hmm.models.get(k).pi = new double[hmm.models.get(k).numStates];

                word = reader.readLine();
                // Load the A, B and Pi matrices
                if (!word.contains("A:")) {
                    System.err
                            .println("loadModelFromFile( fstream &file ) - Could not find the A matrix for the "
                                    + (k + 1) + "th model.");
                    reader.close();
                    return false;
                }

                // Load A
                hmm.models.get(k).a.resize(hmm.models.get(k).numStates,
                        hmm.models.get(k).numStates);
                for (int i = 0; i < hmm.models.get(k).numStates; i++) {
                    word = reader.readLine();
                    System.out.println(word);
                    buf = word.split("\t");
                    for (int j = 0; j < hmm.models.get(k).numStates; j++) {
                        value = Double.parseDouble(buf[j]);
                        hmm.models.get(k).a.dataPtr[i][j] = value;
                    }
                }
                word = reader.readLine();
                if (!word.contains("B:")) {
                    System.err
                            .println("loadModelFromFile( fstream &file ) - Could not find the B matrix for the "
                                    + (k + 1) + "th model.");
                    reader.close();
                    return false;
                }

				// Load B
                // word = reader.readLine();
                hmm.models.get(k).a.resize(hmm.models.get(k).numStates,
                        hmm.models.get(k).numStates);
                for (int i = 0; i < hmm.models.get(k).numStates; i++) {
                    word = reader.readLine();
                    buf = word.split("\t");
                    for (int j = 0; j < hmm.models.get(k).numSymbols; j++) {
                        value = Double.parseDouble(buf[j]);
                        hmm.models.get(k).b.dataPtr[i][j] = value;
                    }
                }

                word = reader.readLine();
                if (!word.contains("Pi:")) {
                    System.err
                            .println("loadModelFromFile( fstream &file ) - Could not find the Pi matrix for the "
                                    + (k + 1) + "th model.");
                    reader.close();
                    return false;
                }

                // Load Pi
                word = reader.readLine();
                buf = word.split("\t");
                for (int i = 0; i < hmm.models.get(k).numStates; i++) {
                    value = Double.parseDouble(buf[i]);
                    hmm.models.get(k).pi[i] = value;
                }
            }

            hmm.maxLikelihood = 0;
            hmm.bestDistance = 0;
            hmm.classLikelihoods = new double[hmm.numClasses];
            hmm.classDistances = new double[hmm.numClasses];
        }
        reader.close();
        return true;
    }
    
    public boolean loadQuantizerFromFile(String file) throws IOException {

		quantizer.initialized = false;
		quantizer.numClusters = 0;
		quantizer.clusters.clear();
		quantizer.quantizationDistances.clear();

		BufferedReader reader;

		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException ex) {
			System.err
					.println("loadDatasetFromFile(String filename) - FILE NOT OPEN!");
			return false;
		}

		String word;

		// Find the file type header
		word = reader.readLine();
		if (!word.contains("KMEANS_QUANTIZER_FILE_V1.0")) {
			System.err
					.println("loadModelFromFile(fstream &file) - Invalid file format!");
			reader.close();
			return false;
		}

		quantizer.numInputDimensions = 3;
		quantizer.numOutputDimensions = 1;
		quantizer.minNumEpochs = 0;
		quantizer.maxNumEpochs = 100;
		quantizer.minChange = 1e-005;

		word = reader.readLine();
		if (!word.contains("QuantizerTrained:")) {
			System.err
					.println("loadModelFromFile(fstream &file) - Failed to load QuantizerTrained!");
			reader.close();
			return false;
		}
		quantizer.trained = (Integer.parseInt(word.split(" ")[1]) == 1);

		word = reader.readLine();
		if (!word.contains("NumClusters:")) {
			System.err
					.println("loadModelFromFile(fstream &file) - Failed to load NumClusters!");
			reader.close();
			return false;
		}
		quantizer.numClusters = Integer.parseInt(word.split(" ")[1]);

		if (quantizer.trained) {
			quantizer.clusters.resize(quantizer.numClusters, quantizer.numInputDimensions);
			word = reader.readLine();
			if (!word.contains("Clusters:")) {
				System.err
						.println("loadModelFromFile(fstream &file) - Failed to load Clusters!");
				reader.close();
				return false;
			}

			String[] buf;
			for (int k = 0; k < quantizer.numClusters; k++) {
				word = reader.readLine();
				buf = word.split("\t");
				for (int j = 0; j < quantizer.numInputDimensions; j++) {
					quantizer.clusters.dataPtr[k][j] = Double.parseDouble(buf[j]);
				}
			}

			quantizer.initialized = true;
			quantizer.featureDataReady = false;
			quantizer.quantizationDistances.ensureCapacity(quantizer.numClusters);
		}
		reader.close();
		return true;
	}
}
