package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;

/* An abstract superclass of outlier sets for various hypotheses. */
public abstract class AbstractOutlierSet implements RoleBasedOutlierSet {

  protected static final String PROP_CONFORMERS = "conformers";

  protected static final String PROP_OUTLIERS = "outliers";

  protected static final String PROP_ROLE = "role";

  /** A lower bound on the probability at which a hypothesis should be considered to be true. */
  private static final double THRESHOLD_PROBABILITY = 2.0 / 3.0;

  /** The nodes that satisfy the hypothesis */
  private SortedSet<String> _conformers;

  /** The nodes that violate the hypothesis */
  private SortedSet<String> _outliers;

  /** An optional role that all of the conformers and outliers play in the network * */
  private String _role;

  @JsonCreator
  public AbstractOutlierSet(
      @JsonProperty(PROP_CONFORMERS) SortedSet<String> conformers,
      @JsonProperty(PROP_OUTLIERS) SortedSet<String> outliers) {
    _conformers = conformers != null ? conformers : ImmutableSortedSet.of();
    _outliers = outliers != null ? outliers : ImmutableSortedSet.of();
  }

  // sort in reverse order of zScore, which is a measure of how likely it is that
  // our hypothesis is correct
  public int compareTo(AbstractOutlierSet other) {
    int oScore = Double.compare(other.outlierScore(), outlierScore());
    if (oScore != 0) {
      return oScore;
    }

    Optional<String> thisRole = getRole();
    Optional<String> otherRole = other.getRole();
    if (thisRole.isPresent()) {
      if (otherRole.isPresent()) {
        return thisRole.get().compareTo(otherRole.get());
      } else {
        return 1;
      }
    } else if (otherRole.isPresent()) {
      return -1;
    } else {
      return 0;
    }
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    } else if (!(other instanceof AbstractOutlierSet)) {
      return false;
    }
    AbstractOutlierSet rhs = (AbstractOutlierSet) other;
    return _conformers.equals(rhs.getConformers())
        && _outliers.equals(rhs.getOutliers())
        && Objects.equals(getRole(), rhs.getRole());
  }

  @Override
  public int hashCode() {
    return Objects.hash(_conformers, _outliers, getRole());
  }

  @JsonProperty(PROP_CONFORMERS)
  @Override
  public SortedSet<String> getConformers() {
    return _conformers;
  }

  @JsonProperty(PROP_OUTLIERS)
  @Override
  public SortedSet<String> getOutliers() {
    return _outliers;
  }

  @JsonProperty(PROP_ROLE)
  @Override
  public Optional<String> getRole() {
    return Optional.ofNullable(_role);
  }

  @JsonProperty(PROP_ROLE)
  @Override
  public void setRole(String role) {
    _role = role;
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
