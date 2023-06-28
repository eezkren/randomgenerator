package com.isilona;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

class RandomGeneratorTest {

  private static final Random random = new Random();

  private static Stream<Arguments> invalidSeedNumberProbabilityProvider() {
    return Stream.of(
        Arguments.of(
            List.of(1, 2),
            List.of(0.2, 0.3, 0.5),
            "Number of seed numbers must match the number of probabilities."
        ),
        Arguments.of(
            List.of(1, 2),
            List.of(0.2, 0.3),
            "Invalid probabilities. The sum of probabilities must be equal to 1."
        ),
        Arguments.of(
            List.of(1, 2),
            List.of(0.2, 1.5),
            "Probabilities must be between 0 and 1 (inclusive)."
        )
    );
  }

  @RepeatedTest(10)
  void returnedNumberShouldBeOneOfTheSeedsWhenListsAreHardcoded() {
    // Given
    List<Integer> seedNumbers = List.of(-1, 0, 1, 2, 3);
    List<Double> probabilities = List.of(0.01, 0.3, 0.58, 0.1, 0.01);
    RandomGenerator generator = new RandomGenerator(seedNumbers, probabilities);

    // When
    int result = generator.nextNum();

    // Then
    assertTrue(seedNumbers.contains(result));
  }

  @RepeatedTest(1000)
  void returnedNumberShouldBeOneOfTheSeedsWhenListsAreGenerated() {
    // Given
    int numberOfSeeds = generateRandomInteger(5, 10);
    List<Integer> seedNumbers = generateRandomSeeds(numberOfSeeds, -numberOfSeeds, numberOfSeeds);
    List<Double> probabilities = generateRandomProbabilityList(numberOfSeeds, 1);
    RandomGenerator generator = new RandomGenerator(seedNumbers, probabilities);

    // When
    int result = generator.nextNum();

    // Then
    assertTrue(seedNumbers.contains(result));
  }

  @RepeatedTest(10)
  void theResultShouldConvergeTheSeedProbabilitiesWhenListsAreHardCoded() {
    // Given
    List<Integer> seedNumbers = List.of(-1, 0, 1, 2, 3);
    List<Double> probabilities = List.of(0.01, 0.3, 0.58, 0.1, 0.01);
    RandomGenerator generator = new RandomGenerator(seedNumbers, probabilities);

    // When
    int numTrials = 1000000;
    int[] counter = new int[seedNumbers.size()];
    for (int i = 0; i < numTrials; i++) {
      int result = generator.nextNum();
      counter[seedNumbers.indexOf(result)]++;
    }

    // Then
    List<Double> observedProbabilities = new ArrayList<>();
    for (int count : counter) {
      double observedProb = (double) count / numTrials;
      observedProbabilities.add(observedProb);
    }

    for (int i = 0; i < probabilities.size(); i++) {
      double observedProb = observedProbabilities.get(i);
      double seedProb = probabilities.get(i);
      assertTrue(Math.abs(observedProb - seedProb) < 0.01);
    }
  }

  @RepeatedTest(10)
  void theResultShouldConvergeTheSeedProbabilitiesWhenListsAreGenerated() {
    // Given
    int numberOfSeeds = generateRandomInteger(5, 10);
    List<Integer> seedNumbers = generateRandomSeeds(numberOfSeeds, -numberOfSeeds, numberOfSeeds);
    List<Double> probabilities = generateRandomProbabilityList(numberOfSeeds, 1);
    RandomGenerator generator = new RandomGenerator(seedNumbers, probabilities);

    // When
    int numTrials = 1000000;
    int[] counter = new int[seedNumbers.size()];
    for (int i = 0; i < numTrials; i++) {
      int result = generator.nextNum();
      counter[seedNumbers.indexOf(result)]++;
    }

    // Then
    List<Double> observedProbabilities = new ArrayList<>();
    for (int count : counter) {
      double observedProb = (double) count / numTrials;
      observedProbabilities.add(observedProb);
    }

    for (int i = 0; i < probabilities.size(); i++) {
      double observedProb = observedProbabilities.get(i);
      double seedProb = probabilities.get(i);
      assertTrue(Math.abs(observedProb - seedProb) < 0.01);
    }
  }

  @ParameterizedTest
  @MethodSource("invalidSeedNumberProbabilityProvider")
  void testInvalidSeedNumberProbability(List<Integer> seedNumbers, List<Double> probabilities, String expectedErrorMessage) {
    // When
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new RandomGenerator(seedNumbers, probabilities));

    // Then
    String actualMessage = exception.getMessage();
    assertTrue(actualMessage.contains(expectedErrorMessage));
  }

  private List<Double> generateRandomProbabilityList(int count, double sum) {
    List<Double> numbers = new ArrayList<>(count);
    BigDecimal remainingSum = BigDecimal.valueOf(sum);

    // Generate random numbers
    for (int i = 0; i < count - 1; i++) {
      double randomNumber = random.nextDouble() * remainingSum.doubleValue();
      BigDecimal roundedNumber = BigDecimal.valueOf(randomNumber).setScale(2, RoundingMode.HALF_UP);
      numbers.add(roundedNumber.doubleValue());
      remainingSum = remainingSum.subtract(roundedNumber);
    }

    // The last number is the remaining sum to ensure the total sum is 1
    BigDecimal roundedSum = remainingSum.setScale(2, RoundingMode.HALF_UP);
    numbers.add(roundedSum.doubleValue());

    return numbers;
  }

  private List<Integer> generateRandomSeeds(int count, int min, int max) {

    if (max - min + 1 < count) {
      throw new IllegalArgumentException("Range is smaller than the required count");
    }

    Set<Integer> generatedSeeds = new HashSet<>();

    while (generatedSeeds.size() < count) {
      int seed = random.nextInt(max - min + 1) + min;
      generatedSeeds.add(seed);
    }

    return new ArrayList<>(generatedSeeds);
  }

  private Integer generateRandomInteger(int min, int max) {
    return random.nextInt(max - min + 1) + min;
  }


}