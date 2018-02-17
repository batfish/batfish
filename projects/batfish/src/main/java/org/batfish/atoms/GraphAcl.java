package org.batfish.atoms;

import org.batfish.datamodel.IpAccessList;

public class GraphAcl {

  private IpAccessList _acl;

  private GraphLink _link;

  private boolean _outbound;

  public GraphAcl(IpAccessList acl, GraphLink link, boolean outbound) {
    this._acl = acl;
    this._link = link;
    this._outbound = outbound;
  }

  public IpAccessList getAcl() {
    return _acl;
  }

  public GraphLink getLink() {
    return _link;
  }

  public boolean isOutbound() {
    return _outbound;
  }
}
