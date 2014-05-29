package batfish.grammar.juniper.firewall;

import java.util.ArrayList;
import java.util.List;

public class ProtocolFromTFFStanza extends FromTFFStanza {
   private List<Integer> _protocols;

   public ProtocolFromTFFStanza() {
      _protocols = new ArrayList<Integer>();
   }

   public void addProtocol(int p) {
      _protocols.add(p);
   }
   
   public List<Integer> getProtocols(){
      return _protocols;
   }

   @Override
   public FromTFFType getType() {
      return FromTFFType.PROTOCOL;
   }

}
