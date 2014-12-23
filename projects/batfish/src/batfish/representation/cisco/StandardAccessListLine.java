package batfish.representation.cisco;

import java.io.Serializable;

import batfish.representation.Ip;
import batfish.representation.IpProtocol;
import batfish.representation.LineAction;

public class StandardAccessListLine implements Serializable {

   private static final long serialVersionUID = 1L;

   private LineAction _action;
   private Ip _ip;
   private Ip _wildcard;

   public StandardAccessListLine(LineAction action, Ip ip, Ip wildcard) {
      _action = action;
      _ip = ip;
      _wildcard = wildcard;
   }

   public LineAction getAction() {
      return _action;
   }

   public Ip getIP() {
      return _ip;
   }

   public Ip getWildcard() {
      return _wildcard;
   }

   public ExtendedAccessListLine toExtendedAccessListLine() {
      return new ExtendedAccessListLine(_action, IpProtocol.IP, _ip, _wildcard,
            null, null, null, null);
   }

}
