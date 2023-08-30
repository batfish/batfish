package org.batfish.representation.juniper;

import com.google.common.annotations.VisibleForTesting;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from destination-prefix-list except */
public final class FwFromDestinationPrefixListExcept implements FwFrom {

  private final String _name;

  public FwFromDestinationPrefixListExcept(String name) {
    _name = name;
  }

  @Override
  public Field getField() {
    return Field.DESTINATION_EXCEPT;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return new MatchHeaderSpace(toHeaderSpace(jc, w), getTraceElement());
  }

  @VisibleForTesting
  HeaderSpace toHeaderSpace(JuniperConfiguration jc, Warnings w) {
    PrefixList pl = jc.getMasterLogicalSystem().getPrefixLists().get(_name);

    if (pl == null) {
      w.redFlag("Reference to undefined destination-prefix-list: \"" + _name + "\"");
      // match nothing
      return HeaderSpace.builder().setNotDstIps(EmptyIpSpace.INSTANCE).build();
    }

    IpSpace space = pl.toIpSpace();
    // if referenced prefix list is empty, it should not match anything
    if (space instanceof EmptyIpSpace && !pl.getHasIpv6()) {
      w.redFlagf("destination-prefix-list \"%s\" is empty", _name);
    }

    return HeaderSpace.builder().setNotDstIps(space).build();
  }

  private TraceElement getTraceElement() {
    return TraceElement.of(String.format("Matched destination-prefix-list %s except", _name));
  }
}
