package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.representation.IpWildcard;
import org.batfish.representation.LineAction;
import org.batfish.representation.TcpFlags;

public class StandardAccessListLine implements Serializable {

   private static final long serialVersionUID = 1L;

   private LineAction _action;

   private Set<Integer> _dscps;

   private Set<Integer> _ecns;

   private IpWildcard _ipWildcard;

   public StandardAccessListLine(LineAction action, IpWildcard ipWildcard,
         Set<Integer> dscps, Set<Integer> ecns) {
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

   public ExtendedAccessListLine toExtendedAccessListLine() {
      return new ExtendedAccessListLine(_action, IpProtocol.IP, _ipWildcard,
            null, IpWildcard.ANY, null, Collections.<SubRange> emptyList(),
            Collections.<SubRange> emptyList(), _dscps, _ecns, null, null,
            Collections.<TcpFlags> emptyList());
   }

}
