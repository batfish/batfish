package org.batfish.geometry;

import org.batfish.datamodel.IpAccessList;

public class AclGraphNode extends GraphNode {

  private IpAccessList _acl;

  public AclGraphNode(String name, int index, IpAccessList acl) {
    super(name, index);
    _acl = acl;
  }

  public IpAccessList getAcl() {
    return _acl;
  }

  @Override
  public String toString() {
    return "AclGraphNode{" + "_name=" + getName() + ", _acl=" + _acl.getName() + '}';
  }
}
