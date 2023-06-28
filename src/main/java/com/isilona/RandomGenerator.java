package com.isilona;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomGenerator {
  private static final double PROBABILITY_TOLERANCE = 0.000001;
  private final List<Integer> seedNumbers;
  private final List<Double> probabilities;
  private final List<Double> cumulativeProbabilities;
  private final Random random;

  public RandomGenerator(List<Integer> seedNumbers, List<Double> probabilities) {
    if (seedNumbers.size() != probabilities.size()) {
      throw new IllegalArgumentException("Number of seed numbers must match the number of probabilities.");
    }
    validateProbabilities(probabilities);
    this.seedNumbers = seedNumbers;
    this.probabilities = probabilities;
    this.cumulativeProbabilities = calculateCumulativeProbabilities();
    this.random = new Random();
  }

  private void validateProbabilities(List<Double> probabilities) {
    double sum = 0.0;
    for (var probability : probabilities) {
      validateProbabilityRange(probability);
      sum += probability;
    }
    if (Math.abs(1.0 - sum) > PROBABILITY_TOLERANCE) {
      throw new IllegalArgumentException("Invalid probabilities. The sum of probabilities must be equal to 1.");
    }
  }

  private void validateProbabilityRange(double probability) {
    if (probability < 0 || probability > 1) {
      throw new IllegalArgumentException("Probabilities must be between 0 and 1 (inclusive).");
    }
  }

  private List<Double> calculateCumulativeProbabilities() {
    List<Double> result = new ArrayList<>(probabilities.size());
    double cumulativeSum = 0.0;

    for (double probability : probabilities) {
      cumulativeSum += probability;
      result.add(cumulativeSum);
    }

    return result;
  }

  public int nextNum() {
    double randomValue = random.nextDouble();
    for (int i = 0; i < cumulativeProbabilities.size(); i++) {
      if (randomValue < cumulativeProbabilities.get(i)) {
        return seedNumbers.get(i);
      }
    }

    throw new IllegalArgumentException("Invalid probabilities. The sum of probabilities must be equal to 1.");
  }
}
