package batfish.representation.juniper;

import java.io.Serializable;
import java.util.List;

public class ASPathAccessList implements Serializable {

   private static final long serialVersionUID = 1L;

   private List<ASPathAccessListLine> _lines;
   private String _name;

   public ASPathAccessList(String name, List<ASPathAccessListLine> lines) {
      _lines = lines;
      _name = name;
   }

   public List<ASPathAccessListLine> getLines() {
      return _lines;
   }

   public String getName() {
      return _name;
   }

}
