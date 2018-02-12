package org.batfish.geometry;

import org.batfish.datamodel.IpAccessList;

public class AclGraphNode extends GraphNode {

  private GraphNode _owner;

  private GraphNode _neighbor;

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

  public GraphNode getNeighborLink() {
    return _neighbor;
  }

  public void setNeighbor(GraphNode neighbor) {
    _neighbor = neighbor;
  }

  public IpAccessList getAcl() {
    return _acl;
  }

  @Override
  public String toString() {
    return "AclGraphNode{" + "_name=" + getName() + ", _acl=" + _acl.getName() + '}';
  }
}
