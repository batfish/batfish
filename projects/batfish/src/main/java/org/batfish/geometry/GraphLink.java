package org.batfish.geometry;

import javax.annotation.Nullable;

public class GraphLink {

  private GraphNode _source;

  private String _sourceIface;

  private GraphNode _target;

  private String _targetIface;

  public GraphLink(
      GraphNode src, String srcIface, @Nullable GraphNode tgt, @Nullable String tgtIface) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GraphLink graphLink = (GraphLink) o;

    if (!_source.equals(graphLink._source)) {
      return false;
    }
    if (!_sourceIface.equals(graphLink._sourceIface)) {
      return false;
    }
    if (_target != null ? !_target.equals(graphLink._target) : graphLink._target != null) {
      return false;
    }
    return _targetIface != null
        ? _targetIface.equals(graphLink._targetIface)
        : graphLink._targetIface == null;
  }

  @Override
  public int hashCode() {
    int result = _source.hashCode();
    result = 31 * result + _sourceIface.hashCode();
    result = 31 * result + (_target != null ? _target.hashCode() : 0);
    result = 31 * result + (_targetIface != null ? _targetIface.hashCode() : 0);
    return result;
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
