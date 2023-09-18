package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from prefix-list */
public final class FwFromPrefixList implements FwFrom {

  private final String _name;

  public FwFromPrefixList(String name) {
    _name = name;
  }

  @Override
  public Field getField() {
    return Field.PREFIX_LIST;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return new MatchHeaderSpace(toHeaderSpace(jc, w), getTraceElement());
  }

  private HeaderSpace toHeaderSpace(JuniperConfiguration jc, Warnings w) {
    PrefixList pl = jc.getMasterLogicalSystem().getPrefixLists().get(_name);

    if (pl == null) {
      w.redFlag("Reference to undefined destination-prefix-list: \"" + _name + "\"");
      // match nothing
      return HeaderSpace.builder().setSrcOrDstIps(EmptyIpSpace.INSTANCE).build();
    }

    IpSpace space = pl.toIpSpace();
    // if referenced prefix list is empty, it should not match anything
    if (space instanceof EmptyIpSpace && !pl.getHasIpv6()) {
      w.redFlagf("prefix-list \"%s\" is empty", _name);
    }

    return HeaderSpace.builder().setSrcOrDstIps(space).build();
  }

  private TraceElement getTraceElement() {
    return TraceElement.of(String.format("Matched prefix-list %s", _name));
  }
}
