package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.State;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlags;

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
    return new ExtendedAccessListLine(
        _name,
        _action,
        IpProtocol.IP,
        _ipWildcard,
        null,
        IpWildcard.ANY,
        null,
        Collections.<SubRange>emptyList(),
        Collections.<SubRange>emptyList(),
        _dscps,
        _ecns,
        null,
        null,
        EnumSet.noneOf(State.class),
        Collections.<TcpFlags>emptyList());
  }
}
