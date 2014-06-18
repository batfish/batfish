package batfish.grammar.juniper.firewall;

import java.util.ArrayList;
import java.util.List;

import batfish.util.SubRange;

public class SourcePortFromTFFStanza extends FromTFFStanza {
private List<SubRange> _ports;
   
   public SourcePortFromTFFStanza(){
      _ports = new ArrayList<SubRange>();
   }
   
   public void addPort(int p){
      _ports.add(new SubRange(p,p));
   }
   
   public List<SubRange> getPorts(){
      return _ports;
   }

   @Override
   public FromTFFType getType() {
      return FromTFFType.SOURCE_PORT;
   }

}
