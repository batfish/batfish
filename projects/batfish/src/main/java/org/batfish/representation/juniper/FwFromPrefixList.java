package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.RouteFilterList;

public final class FwFromPrefixList extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final String _name;

  public FwFromPrefixList(String name) {
    _name = name;
  }

  @Override
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    PrefixList pl = jc.getMasterLogicalSystem().getPrefixLists().get(_name);
    if (pl != null) {
      if (pl.getIpv6()) {
        return;
      }
      RouteFilterList prefixList = c.getRouteFilterLists().get(_name);

      // if referenced prefix list is empty, it should not match anything
      if (prefixList.getLines().isEmpty()) {
        headerSpaceBuilder.addSrcOrDstIp(EmptyIpSpace.INSTANCE);
        return;
      }

      headerSpaceBuilder.addSrcOrDstIp(
          AclIpSpace.union(
              prefixList
                  .getMatchingIps()
                  .stream()
                  .map(IpWildcard::toIpSpace)
                  .collect(ImmutableList.toImmutableList())));
    } else {
      w.redFlag("Reference to undefined prefix-list: \"" + _name + "\"");
    }
  }
}
