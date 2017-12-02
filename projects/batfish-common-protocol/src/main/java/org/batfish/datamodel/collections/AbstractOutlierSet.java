package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedSet;

/* An abstract superclass of outlier sets for various hypotheses. */
public abstract class AbstractOutlierSet {

  protected static final String PROP_CONFORMERS = "conformers";

  protected static final String PROP_OUTLIERS = "outliers";

  /** A lower bound on the probability at which a hypothesis should be considered to be true. */
  private static final double THRESHOLD_PROBABILITY = 2.0 / 3.0;

  /** The nodes that satisfy the hypothesis */
  private SortedSet<String> _conformers;

  /** The nodes that violate the hypothesis */
  private SortedSet<String> _outliers;

  @JsonCreator
  public AbstractOutlierSet(
      @JsonProperty(PROP_CONFORMERS) SortedSet<String> conformers,
      @JsonProperty(PROP_OUTLIERS) SortedSet<String> outliers) {
    _conformers = conformers;
    _outliers = outliers;
  }

  @JsonProperty(PROP_CONFORMERS)
  public SortedSet<String> getConformers() {
    return _conformers;
  }

  @JsonProperty(PROP_OUTLIERS)
  public SortedSet<String> getOutliers() {
    return _outliers;
  }

  /*
  A standard z statistic computation.
  Intuitively it counts how many standard deviations away this outlier set is from the expected
  value, which is defined by the THRESHOLD_PROBABILITY.
  */
  private double zScore() {
    double e = _conformers.size();
    double n = e + _outliers.size();
    double numerator = e / n - THRESHOLD_PROBABILITY;
    double denominator = Math.sqrt(THRESHOLD_PROBABILITY * (1.0 - THRESHOLD_PROBABILITY) / n);
    return numerator / denominator;
  }

  /* A metric indicating how likely this outlier set represents an actual bug.
    A higher score means that it is more likely to be a bug.
    This metric is used to rank outliers for presentation to users.
  */
  public double outlierScore() {
    return zScore();
  }
}
