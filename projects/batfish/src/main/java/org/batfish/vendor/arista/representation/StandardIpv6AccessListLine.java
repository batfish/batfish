package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import org.batfish.datamodel.Ip6Wildcard;
import org.batfish.datamodel.LineAction;

public class StandardIpv6AccessListLine implements Serializable {

  private final LineAction _action;

  private final Set<Integer> _dscps;

  private final Set<Integer> _ecns;

  private final Ip6Wildcard _ipWildcard;

  private final String _name;

  public StandardIpv6AccessListLine(
      String name,
      LineAction action,
      Ip6Wildcard ipWildcard,
      Set<Integer> dscps,
      Set<Integer> ecns) {
    _name = name;
    _action = action;
    _ipWildcard = ipWildcard;
    _dscps = dscps;
    _ecns = ecns;
  }

  public LineAction getAction() {
    return _action;
  }

  public Ip6Wildcard getIpWildcard() {
    return _ipWildcard;
  }

  public String getName() {
    return _name;
  }

  public ExtendedIpv6AccessListLine toExtendedIpv6AccessListLine() {
    return new ExtendedIpv6AccessListLine(
        _name,
        _action,
        null,
        _ipWildcard,
        Ip6Wildcard.ANY,
        Collections.emptyList(),
        Collections.emptyList(),
        _dscps,
        _ecns,
        null,
        null,
        Collections.emptyList());
  }
}
