package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;
import org.batfish.common.util.CommonUtil;

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

  public NamedStructureEquivalenceSet(Iterable<String> nodes, T namedStructure) {
    _namedStructure = namedStructure;
    _nodes = ImmutableSortedSet.copyOf(nodes);
  }

  public boolean compareStructure(T s) {
    if (_namedStructure == null) {
      return s == null;
    } else {
      return (s != null) && CommonUtil.checkJsonEqual(_namedStructure, s);
    }
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

  public String prettyPrint(String indent) {
    return String.format("%s%s\n", indent, String.join(" ", _nodes));
  }

  public void setNamedStructure(T namedStructure) {
    _namedStructure = namedStructure;
  }

  @JsonProperty(PROP_NODES)
  public void setNodes(SortedSet<String> nodes) {
    _nodes = nodes;
  }

  @JsonProperty(PROP_REPRESENTATIVE_ELEMENT)
  private void setRepresentativeElement(@SuppressWarnings("unused") String representativeElement) {
    // No body because this is a virtual property computed from nodes
  }

  @Override
  public String toString() {
    return _nodes.toString();
  }
}
