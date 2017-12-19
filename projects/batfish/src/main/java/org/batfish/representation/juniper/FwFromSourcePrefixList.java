package org.batfish.representation.juniper;

import com.google.common.collect.Iterables;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.RouteFilterList;

public final class FwFromSourcePrefixList extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final String _name;

  public FwFromSourcePrefixList(String name) {
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
      List<IpWildcard> wildcards = sourcePrefixList.getMatchingIps();
      line.setSrcIps(Iterables.concat(line.getSrcIps(), wildcards));
    } else {
      w.redFlag("Reference to undefined source prefix-list: \"" + _name + "\"");
    }
  }
}
