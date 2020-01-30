package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.RouteFilterList;
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
    return new MatchHeaderSpace(toHeaderspace(jc, c, w), getTraceElement());
  }

  private HeaderSpace toHeaderspace(JuniperConfiguration jc, Configuration c, Warnings w) {
    PrefixList pl = jc.getMasterLogicalSystem().getPrefixLists().get(_name);

    if (pl == null) {
      w.redFlag("Reference to undefined destination-prefix-list: \"" + _name + "\"");
      // match nothing
      return HeaderSpace.builder().setSrcOrDstIps(EmptyIpSpace.INSTANCE).build();
    }

    if (pl.getIpv6()) {
      // do not handle Ipv6 for now, assume matching nothing
      return HeaderSpace.builder().setSrcOrDstIps(EmptyIpSpace.INSTANCE).build();
    }

    RouteFilterList prefixList = c.getRouteFilterLists().get(_name);

    // if referenced prefix list is empty, it should not match anything
    if (prefixList.getLines().isEmpty()) {
      return HeaderSpace.builder().setSrcOrDstIps(EmptyIpSpace.INSTANCE).build();
    }

    return HeaderSpace.builder()
        .setSrcOrDstIps(
            AclIpSpace.union(
                prefixList.getMatchingIps().stream()
                    .map(IpWildcard::toIpSpace)
                    .collect(ImmutableList.toImmutableList())))
        .build();
  }

  private TraceElement getTraceElement() {
    return TraceElement.of(String.format("Matched prefix-list %s", _name));
  }
}
