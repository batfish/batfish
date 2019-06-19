package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.RouteFilterList;

public class FwFromSourcePrefixListExcept extends FwFrom {

  private static final long serialVersionUID = 1L;

  private final String _name;

  public FwFromSourcePrefixListExcept(String name) {
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
      RouteFilterList sourcePrefixList = c.getRouteFilterLists().get(_name);

      // if referenced prefix list is empty, it should not match anything
      if (sourcePrefixList.getLines().isEmpty()) {
        headerSpaceBuilder.addNotSrcIp(EmptyIpSpace.INSTANCE);
        return;
      }

      headerSpaceBuilder.addNotSrcIp(
          AclIpSpace.union(
              sourcePrefixList.getMatchingIps().stream()
                  .map(IpWildcard::toIpSpace)
                  .collect(ImmutableList.toImmutableList())));
    } else {
      w.redFlag("Reference to undefined source prefix-list: \"" + _name + "\"");
    }
  }
}
