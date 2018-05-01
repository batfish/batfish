package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.RouteFilterList;

public final class FwFromDestinationPrefixList extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final String _name;

  public FwFromDestinationPrefixList(String name) {
    _name = name;
  }

  @Override
  public void applyTo(
      HeaderSpace.Builder headerSpaceBuilder,
      JuniperConfiguration jc,
      Warnings w,
      Configuration c) {
    PrefixList pl = jc.getPrefixLists().get(_name);
    if (pl != null) {
      pl.getReferers().put(this, "firewall from destination-prefix-list");
      if (pl.getIpv6()) {
        return;
      }
      RouteFilterList destinationPrefixList = c.getRouteFilterLists().get(_name);
      List<IpSpace> wildcards =
          destinationPrefixList
              .getMatchingIps()
              .stream()
              .map(IpWildcard::toIpSpace)
              .collect(ImmutableList.toImmutableList());

      ImmutableList.Builder<IpSpace> ipSpaceBuilder = ImmutableList.builder();
      if (headerSpaceBuilder.getDstIps() != null) {
        ipSpaceBuilder.add(headerSpaceBuilder.getDstIps());
      }
      headerSpaceBuilder.setDstIps(AclIpSpace.union(ipSpaceBuilder.addAll(wildcards).build()));
    } else {
      w.redFlag("Reference to undefined source prefix-list: \"" + _name + "\"");
    }
  }
}
