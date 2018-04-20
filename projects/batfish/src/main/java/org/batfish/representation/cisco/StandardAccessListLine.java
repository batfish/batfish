package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.Set;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;

public class StandardAccessListLine implements Serializable {

  private static final long serialVersionUID = 1L;

  private final LineAction _action;

  private final Set<Integer> _dscps;

  private final Set<Integer> _ecns;

  private final IpWildcard _ipWildcard;

  private final String _name;

  public StandardAccessListLine(
      String name,
      LineAction action,
      IpWildcard ipWildcard,
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

  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  public String getName() {
    return _name;
  }

  public ExtendedAccessListLine toExtendedAccessListLine() {
    return ExtendedAccessListLine.builder()
        .setAction(_action)
        .setDstAddressSpecifier(new WildcardAddressSpecifier(IpWildcard.ANY))
        .setName(_name)
        .setServiceSpecifier(
            SimpleStandardServiceSpecifier.builder()
                .setProtocol(IpProtocol.IP)
                .setDscps(_dscps)
                .setEcns(_ecns)
                .build())
        .setSrcAddressSpecifier(new WildcardAddressSpecifier(_ipWildcard))
        .build();
  }
}
