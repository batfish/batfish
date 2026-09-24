package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from learn-vlan-id. */
public final class FwFromLearnVlanId implements FwFrom {

  private final int _vlanId;

  public FwFromLearnVlanId(int vlanId) {
    _vlanId = vlanId;
  }

  @Override
  public Field getField() {
    return Field.LEARN_VLAN_ID;
  }

  public int getVlanId() {
    return _vlanId;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return new FalseExpr(TraceElement.of(String.format("Matched learn-vlan-id %d", _vlanId)));
  }
}
