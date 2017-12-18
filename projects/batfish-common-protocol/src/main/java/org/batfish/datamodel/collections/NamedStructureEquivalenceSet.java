package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;
import org.batfish.common.util.CommonUtil;

public class NamedStructureEquivalenceSet<T>
    implements Comparable<NamedStructureEquivalenceSet<T>> {

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

  public NamedStructureEquivalenceSet(T namedStructure, SortedSet<String> nodes) {
    _namedStructure = namedStructure;
    _nodes = nodes;
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

  public SortedSet<String> getNodes() {
    return _nodes;
  }

  @JsonProperty(PROP_REPRESENTATIVE_ELEMENT)
  public String getRepresentativeElement() {
    return _nodes.first();
  }

  public String prettyPrint(String indent) {
    String representativeElement = getRepresentativeElement();
    StringBuilder sb = new StringBuilder(indent + representativeElement);
    for (String node : _nodes) {
      if (!node.equals(representativeElement)) {
        sb.append(" " + node);
      }
    }
    sb.append("\n");
    return sb.toString();
  }

  public void setNamedStructure(T namedStructure) {
    _namedStructure = namedStructure;
  }

  public void setNodes(SortedSet<String> nodes) {
    _nodes = nodes;
  }

  @JsonProperty(PROP_REPRESENTATIVE_ELEMENT)
  private void setRepresentativeElement(String representativeElement) {}

  @Override
  public String toString() {
    return _nodes.toString();
  }
}
