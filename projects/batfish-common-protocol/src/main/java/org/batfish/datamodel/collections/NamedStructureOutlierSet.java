package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.SortedSet;

public class NamedStructureOutlierSet<T> implements Comparable<NamedStructureOutlierSet<T>> {

  private static final String PROP_NAME = "name";

  private static final String PROP_STRUCT_TYPE = "structType";

  private static final String PROP_NAMED_STRUCT_TYPES = "namedStructTypes";

  private static final String PROP_CONFORMERS = "conformers";

  private static final String PROP_OUTLIERS = "outliers";

  /** A lower bound on the probability at which a hypothesis should be considered to be true */
  private static final double THRESHOLD_PROBABILITY = 0.9;

  /** The nodes that satisfy the above hypothesis */
  private SortedSet<String> _conformers;

  /** The name of the structure */
  private String _name;

  /**
   * The structure definition that is hypothesized to be the correct one. If null, represents a
   * hypothesis that no such named structure should exist in a node.
   */
  private T _namedStructure;

  /** The nodes that violate the above hypothesis */
  private SortedSet<String> _outliers;

  /** The name of the structure type (e.g., CommunityList, IpAccessList) */
  private String _structType;

  @JsonCreator
  public NamedStructureOutlierSet(
      @JsonProperty(PROP_STRUCT_TYPE) String structType,
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_NAMED_STRUCT_TYPES) T namedStructure,
      @JsonProperty(PROP_CONFORMERS) SortedSet<String> conformers,
      @JsonProperty(PROP_OUTLIERS) SortedSet<String> outliers) {
    _structType = structType;
    _name = name;
    _namedStructure = namedStructure;
    _conformers = conformers;
    _outliers = outliers;
  }

  @Override
  // sort in reverse order of zScore, which is a measure of how likely it is that
  // our hypothesis is correct
  public int compareTo(NamedStructureOutlierSet<T> other) {
    int zComp = Double.compare(other.zScore(), this.zScore());
    if (zComp != 0) {
      return zComp;
    }
    int structComp = _structType.compareTo(other.getStructType());
    if (structComp != 0) {
      return structComp;
    }
    return _name.compareTo(other.getName());
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof NamedStructureOutlierSet)) {
      return false;
    }
    NamedStructureOutlierSet<?> rhs = (NamedStructureOutlierSet<?>) o;
    return _structType.equals(rhs.getStructType()) && _name.equals(rhs.getName());
  }

  public SortedSet<String> getConformers() {
    return _conformers;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  // ignore for now to avoid encoding large amounts of information in answer
  @JsonIgnore
  public T getNamedStructure() {
    return _namedStructure;
  }

  public SortedSet<String> getOutliers() {
    return _outliers;
  }

  @JsonProperty(PROP_STRUCT_TYPE)
  public String getStructType() {
    return _structType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_structType, _name);
  }

  public double zScore() {
    double e = _conformers.size();
    double n = e + _outliers.size();
    double numerator = e / n - THRESHOLD_PROBABILITY;
    double denominator = Math.sqrt(THRESHOLD_PROBABILITY * (1.0 - THRESHOLD_PROBABILITY) / n);
    return numerator / denominator;
  }
}
