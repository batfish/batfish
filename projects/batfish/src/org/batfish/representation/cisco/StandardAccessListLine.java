package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.batfish.common.datamodel.Ip;
import org.batfish.common.datamodel.IpProtocol;
import org.batfish.common.util.SubRange;
import org.batfish.representation.LineAction;
import org.batfish.representation.TcpFlags;

public class StandardAccessListLine implements Serializable {

   private static final long serialVersionUID = 1L;

   private LineAction _action;

   private Set<Integer> _dscps;

   private Set<Integer> _ecns;

   private Ip _ip;

   private Ip _wildcard;

   public StandardAccessListLine(LineAction action, Ip ip, Ip wildcard,
         Set<Integer> dscps, Set<Integer> ecns) {
      _action = action;
      _ip = ip;
      _wildcard = wildcard;
      _dscps = dscps;
      _ecns = ecns;
   }

   public LineAction getAction() {
      return _action;
   }

   public Ip getIp() {
      return _ip;
   }

   public Ip getWildcard() {
      return _wildcard;
   }

   public ExtendedAccessListLine toExtendedAccessListLine() {
      return new ExtendedAccessListLine(_action, IpProtocol.IP, _ip, _wildcard,
            null, Ip.ZERO, Ip.MAX, null, Collections.<SubRange> emptyList(),
            Collections.<SubRange> emptyList(), _dscps, _ecns, null, null,
            Collections.<TcpFlags> emptyList());
   }

}
