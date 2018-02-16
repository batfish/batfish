package org.batfish.atoms;

import java.util.BitSet;

public class PortReachabilitySummary {

  private Path _path;

  private BitSet _forwarding;

  private BitSet _acl;

  public PortReachabilitySummary(Path path, BitSet forwarding, BitSet acl) {
    this._path = path;
    this._forwarding = forwarding;
    this._acl = acl;
  }

  public PortReachabilitySummary applyPort(BitSet fwd, BitSet acl) {
    BitSet newFwd = (BitSet) _forwarding.clone();
    BitSet newAcl = (BitSet) _acl.clone();
    newFwd.and(fwd);
    newAcl.and(acl);
    return new PortReachabilitySummary(_path, newFwd, newAcl);
  }

  public PortReachabilitySummary extendBy(GraphLink link) {
    Path p = new Path(_path.getLinks().plus(link), _path.getSource(), link.getTarget());
    return new PortReachabilitySummary(p, _forwarding, _acl);
  }

  public BitSet getForwarding() {
    return _forwarding;
  }

  public BitSet getAcl() {
    return _acl;
  }

  public Path getPath() {
    return _path;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PortReachabilitySummary that = (PortReachabilitySummary) o;

    if (!_path.equals(that._path)) {
      return false;
    }
    if (!_forwarding.equals(that._forwarding)) {
      return false;
    }
    return _acl.equals(that._acl);
  }

  @Override
  public int hashCode() {
    int result = _path.hashCode();
    result = 31 * result + _forwarding.hashCode();
    result = 31 * result + _acl.hashCode();
    return result;
  }
}
