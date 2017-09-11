package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.util.CommonUtil;

public class NamedStructureEquivalenceSet<T>
    implements Comparable<NamedStructureEquivalenceSet<T>> {

  private static final String PROP_REPRESENTATIVE_ELEMENT = "representativeElement";

  // a null _namedStructure represents an equivalence class for nodes that are missing
  // a structure of a given name
  private T _namedStructure;

  private SortedSet<String> _nodes;

  private final String _representativeElement;

  @JsonCreator
  public NamedStructureEquivalenceSet(
      @JsonProperty(PROP_REPRESENTATIVE_ELEMENT) String representativeElement) {
    _representativeElement = representativeElement;
  }

  public NamedStructureEquivalenceSet(String node, T namedStructure) {
    this(node);
    _namedStructure = namedStructure;
    _nodes = new TreeSet<>();
    _nodes.add(node);
  }

  public boolean compareStructure(T s) {
    if (_namedStructure == null) {
      return (s == null);
    } else {
      return (s != null) && CommonUtil.checkJsonEqual(_namedStructure, s);
    }
  }

  @Override
  public int compareTo(NamedStructureEquivalenceSet<T> rhs) {
    return _representativeElement.compareTo(rhs._representativeElement);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof NamedStructureEquivalenceSet)) {
      return false;
    }
    NamedStructureEquivalenceSet<?> rhs = (NamedStructureEquivalenceSet<?>) o;
    return _representativeElement.equals(rhs._representativeElement);
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
    return _representativeElement;
  }

  @Override
  public int hashCode() {
    return _representativeElement.hashCode();
  }

  public String prettyPrint(String indent) {
    StringBuilder sb = new StringBuilder(indent + _representativeElement);
    for (String node : _nodes) {
      if (!node.equals(_representativeElement)) {
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
}
