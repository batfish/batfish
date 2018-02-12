package org.batfish.geometry;

import org.batfish.datamodel.IpAccessList;

public class AclGraphNode extends GraphNode {

  private GraphNode _owner;

  private GraphLink _ownerLink;

  private IpAccessList _acl;

  public AclGraphNode(String name, int index, IpAccessList acl, GraphNode owner) {
    super(name, index);
    _acl = acl;
    _owner = owner;
  }

  @Override
  public GraphNode owner() {
    return _owner;
  }

  public GraphLink getOwnerLink() {
    return _ownerLink;
  }

  public void setOwnerLink(GraphLink ownerLink) {
    _ownerLink = ownerLink;
  }

  public IpAccessList getAcl() {
    return _acl;
  }

  @Override
  public String toString() {
    return "AclGraphNode{" + "_name=" + getName() + ", _acl=" + _acl.getName() + '}';
  }
}
