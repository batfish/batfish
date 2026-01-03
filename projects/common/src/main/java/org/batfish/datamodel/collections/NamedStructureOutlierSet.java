package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.SortedSet;
import org.batfish.role.OutliersHypothesis;

public class NamedStructureOutlierSet<T> extends AbstractOutlierSet
    implements Comparable<NamedStructureOutlierSet<T>> {
  private static final String PROP_HYPOTHESIS = "hypothesis";
  private static final String PROP_NAME = "name";
  private static final String PROP_STRUCT_TYPE = "structType";
  private static final String PROP_STRUCT_DEFINITION = "structDefinition";

  // the hypothesis used to identify outliers
  private OutliersHypothesis _hypothesis;

  /**
   * The name of the structure type (e.g., CommunityList, IpAccessList) for which we are performing
   * outlier detection
   */
  private String _structType;

  /** The name of the structure for which we are performing outlier detection */
  private String _name;

  /**
   * If the hypothesis is SAME_DEFINITION, this field contains the structure definition that is
   * hypothesized to be the correct one. If the hypothesis is SAME_NAME, this field is non-null if
   * the hypothesis is that a structure of this name should exist and null if the hypothesis is that
   * a structure of this name should not exist.
   */
  private T _namedStructure;

  @JsonCreator
  public NamedStructureOutlierSet(
      @JsonProperty(PROP_HYPOTHESIS) OutliersHypothesis hypothesis,
      @JsonProperty(PROP_STRUCT_TYPE) String structType,
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_STRUCT_DEFINITION) T namedStructure,
      @JsonProperty(PROP_CONFORMERS) SortedSet<String> conformers,
      @JsonProperty(PROP_OUTLIERS) SortedSet<String> outliers) {
    super(conformers, outliers);
    _hypothesis = hypothesis;
    _structType = structType;
    _name = name;
    _namedStructure = namedStructure;
  }

  // sort in reverse order of zScore, which is a measure of how likely it is that
  // our hypothesis is correct
  @Override
  public int compareTo(NamedStructureOutlierSet<T> other) {
    int superScore = super.compareTo(other);
    if (superScore != 0) {
      return superScore;
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
    return super.equals(rhs)
        && _structType.equals(rhs.getStructType())
        && _name.equals(rhs.getName());
  }

  @JsonProperty(PROP_HYPOTHESIS)
  public OutliersHypothesis getHypothesis() {
    return _hypothesis;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  // ignore for now to avoid encoding large amounts of information in answer
  @JsonProperty(PROP_STRUCT_DEFINITION)
  public T getNamedStructure() {
    return _namedStructure;
  }

  @JsonProperty(PROP_STRUCT_TYPE)
  public String getStructType() {
    return _structType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_structType, _name);
  }
}
