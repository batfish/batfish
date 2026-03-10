package org.batfish.representation.juniper;

import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;

import com.google.common.annotations.VisibleForTesting;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.representation.juniper.FwTerm.Field;

/** Class for firewall filter from source-prefix-list */
public final class FwFromSourcePrefixList implements FwFrom {

  private final String _name;

  public FwFromSourcePrefixList(String name) {
    _name = name;
  }

  @Override
  public Field getField() {
    return Field.SOURCE;
  }

  @Override
  public AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w) {
    return matchSrc(toIpSpace(jc, w), getTraceElement());
  }

  @VisibleForTesting
  IpSpace toIpSpace(JuniperConfiguration jc, Warnings w) {
    PrefixList pl = jc.getMasterLogicalSystem().getPrefixLists().get(_name);

    if (pl == null) {
      w.redFlag("Reference to undefined source-prefix-list: \"" + _name + "\"");
      // match nothing
      return EmptyIpSpace.INSTANCE;
    }

    IpSpace space = pl.toIpSpace();
    // if referenced prefix list is empty, it should not match anything
    if (space instanceof EmptyIpSpace && !pl.getHasIpv6()) {
      w.redFlagf("source-prefix-list \"%s\" is empty", _name);
    }

    return space;
  }

  private TraceElement getTraceElement() {
    return TraceElement.of(String.format("Matched source-prefix-list %s", _name));
  }
}
