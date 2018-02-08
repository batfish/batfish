package org.batfish.geometry;

import org.batfish.datamodel.IpAccessList;

public class AclGraphNode extends GraphNode {

  private IpAccessList _acl;

  public AclGraphNode(String name, IpAccessList acl) {
    super(name);
    _acl = acl;
  }

  public IpAccessList getAcl() {
    return _acl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    AclGraphNode that = (AclGraphNode) o;

    return _acl != null ? _acl.getName().equals(that._acl.getName()) : that._acl == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (_acl != null ? _acl.getName().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "AclGraphNode{" + "_name=" + getName() + ", _acl=" + _acl.getName() + '}';
  }
}
