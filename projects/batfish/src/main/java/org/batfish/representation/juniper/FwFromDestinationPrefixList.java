package org.batfish.representation.juniper;

import com.google.common.annotations.VisibleForTesting;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter destination-prefix-list */
public final class FwFromDestinationPrefixList implements FwFrom {

  private final String _name;

  public FwFromDestinationPrefixList(String name) {
    _name = name;
  }

  @Override
  public Field getField() {
    return Field.DESTINATION;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return AclLineMatchExprs.matchDst(toIpSpace(jc, w), getTraceElement());
  }

  @VisibleForTesting
  IpSpace toIpSpace(JuniperConfiguration jc, Warnings w) {
    PrefixList pl = jc.getMasterLogicalSystem().getPrefixLists().get(_name);

    if (pl == null) {
      w.redFlag("Reference to undefined destination-prefix-list: \"" + _name + "\"");
      // match nothing
      return EmptyIpSpace.INSTANCE;
    }

    IpSpace space = pl.toIpSpace();
    // if referenced prefix list is empty, it should not match anything
    if (space instanceof EmptyIpSpace && !pl.getHasIpv6()) {
      w.redFlagf("destination-prefix-list \"%s\" is empty", _name);
    }

    return space;
  }

  private TraceElement getTraceElement() {
    return TraceElement.of(String.format("Matched destination-prefix-list %s", _name));
  }
}
