package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;

@SuppressWarnings("PMD.OverrideBothEqualsAndHashCodeOnComparable")
public class NamedStructureEquivalenceSet<T>
    implements Comparable<NamedStructureEquivalenceSet<T>> {
  private static final String PROP_NODES = "nodes";
  private static final String PROP_REPRESENTATIVE_ELEMENT = "representativeElement";

  // a null _namedStructure represents an equivalence class for nodes that are missing
  // a structure of a given name
  private T _namedStructure;

  private SortedSet<String> _nodes;

  @JsonCreator
  private NamedStructureEquivalenceSet() {}

  public NamedStructureEquivalenceSet(String node, T namedStructure) {
    _namedStructure = namedStructure;
    _nodes = ImmutableSortedSet.of(node);
  }

  @Override
  public int compareTo(NamedStructureEquivalenceSet<T> rhs) {
    return getRepresentativeElement().compareTo(rhs.getRepresentativeElement());
  }

  // ignore for now to avoid encoding large amounts of information in answer
  @JsonIgnore
  public T getNamedStructure() {
    return _namedStructure;
  }

  @JsonProperty(PROP_NODES)
  public SortedSet<String> getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_REPRESENTATIVE_ELEMENT)
  public String getRepresentativeElement() {
    return _nodes.first();
  }

  @JsonProperty(PROP_NODES)
  public void setNodes(SortedSet<String> nodes) {
    _nodes = nodes;
  }

  @JsonProperty(PROP_REPRESENTATIVE_ELEMENT)
  private void setRepresentativeElement(String ignoredRepresentativeElement) {
    // No body because this is a virtual property computed from nodes
  }

  @Override
  public String toString() {
    return _nodes.toString();
  }
}
