package org.batfish.geometry;

import javax.annotation.Nullable;

public class GraphLink {

  private int _index;

  private GraphNode _source;

  private String _sourceIface;

  private GraphNode _target;

  private String _targetIface;

  public GraphLink(
      GraphNode src,
      String srcIface,
      @Nullable GraphNode tgt,
      @Nullable String tgtIface,
      int index) {
    this._index = index;
    this._source = src;
    this._sourceIface = srcIface;
    this._target = tgt;
    this._targetIface = tgtIface;
  }

  public GraphNode getSource() {
    return _source;
  }

  public String getSourceIface() {
    return _sourceIface;
  }

  public GraphNode getTarget() {
    return _target;
  }

  public String getTargetIface() {
    return _targetIface;
  }

  public int getIndex() {
    return _index;
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GraphLink graphLink = (GraphLink) o;

    return _index == graphLink._index;
  }

  @Override public int hashCode() {
    return _index;
  }

  @Override
  public String toString() {
    return "GraphLink{"
        + "_source="
        + _source
        + ", _sourceIface='"
        + _sourceIface
        + '\''
        + ", _target="
        + _target
        + ", _targetIface='"
        + _targetIface
        + '\''
        + '}';
  }
}
