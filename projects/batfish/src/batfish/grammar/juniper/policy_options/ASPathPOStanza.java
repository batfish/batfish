package batfish.grammar.juniper.policy_options;

import java.util.ArrayList;
import java.util.List;

import batfish.representation.juniper.ASPathAccessListLine;

public class ASPathPOStanza extends POStanza {
   private String _name;
   private List<ASPathAccessListLine> _line;

   public ASPathPOStanza(String n) {
      _name = n; 
      _line = new ArrayList<ASPathAccessListLine>();
   }

   public void addMember(String m) {
      ASPathAccessListLine l = new ASPathAccessListLine(m);
      _line.add(l);
   }

   public String getName() {
      return _name;
   }

   public List<ASPathAccessListLine> getLines() {
      return _line;
   }

   @Override
   public POType getType() {
      return POType.AS_PATH;
   }

}
