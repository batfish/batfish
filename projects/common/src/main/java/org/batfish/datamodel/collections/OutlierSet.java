package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.SortedSet;

public class OutlierSet<T> extends AbstractOutlierSet implements Comparable<OutlierSet<T>> {
  private static final String PROP_NAME = "name";
  private static final String PROP_DEFINITION = "definition";

  /** The name of the configuration property for which we are performing outlier detection */
  private String _name;

  /** The definition that is hypothesized to be the correct definition of that property. */
  private T _definition;

  @JsonCreator
  public OutlierSet(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_DEFINITION) T definition,
      @JsonProperty(PROP_CONFORMERS) SortedSet<String> conformers,
      @JsonProperty(PROP_OUTLIERS) SortedSet<String> outliers) {
    super(conformers, outliers);
    _name = name;
    _definition = definition;
  }

  @Override
  // sort in reverse order of zScore, which is a measure of how likely it is that
  // our hypothesis is correct
  public int compareTo(OutlierSet<T> other) {
    int superScore = super.compareTo(other);
    if (superScore != 0) {
      return superScore;
    }

    return _name.compareTo(other.getName());
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof OutlierSet)) {
      return false;
    }
    OutlierSet<?> rhs = (OutlierSet<?>) o;
    return super.equals(rhs) && _name.equals(rhs.getName());
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_DEFINITION)
  public T getDefinition() {
    return _definition;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_definition, _name);
  }
}
