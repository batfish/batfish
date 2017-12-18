package org.batfish.representation.juniper;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.stream.Collectors;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.RouteFilterList;

public final class FwFromPrefixList extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final String _name;

  public FwFromPrefixList(String name) {
    _name = name;
  }

  @Override
  public void applyTo(IpAccessListLine line, JuniperConfiguration jc, Warnings w, Configuration c) {
    PrefixList pl = jc.getPrefixLists().get(_name);
    if (pl != null) {
      pl.getReferers().put(this, "firewall from source-prefix-list");
      if (pl.getIpv6()) {
        return;
      }
      RouteFilterList sourcePrefixList = c.getRouteFilterLists().get(_name);
      List<IpWildcard> wildcards =
          sourcePrefixList
              .getLines()
              .stream()
              .map(
                  rfLine -> {
                    if (rfLine.getAction() != LineAction.ACCEPT) {
                      throw new BatfishException(
                          "Expected accept action for routerfilterlist from juniper");
                    } else {
                      return new IpWildcard(rfLine.getPrefix());
                    }
                  })
              .collect(Collectors.toList());
      line.setSrcOrDstIps(Iterables.concat(line.getSrcOrDstIps(), wildcards));
    } else {
      w.redFlag("Reference to undefined source prefix-list: \"" + _name + "\"");
    }
  }
}
