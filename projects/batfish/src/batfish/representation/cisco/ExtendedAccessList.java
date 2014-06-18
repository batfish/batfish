package batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;

public class ExtendedAccessList {
   private List<ExtendedAccessListLine> _lines;
   private String _id;

   public ExtendedAccessList(String id) {
      _id = id;
      _lines = new ArrayList<ExtendedAccessListLine>();
//      _lines.add(new ExtendedAccessListLine(LineAction.REJECT, 0,
//            "0.0.0.0", "255.255.255.255", "0.0.0.0", "255.255.255.255", null));
   }

   public String getId() {
      return _id;
   }

   public void addLine(ExtendedAccessListLine all) {
      _lines.add(all);
   }

   public List<ExtendedAccessListLine> getLines() {
      return _lines;
   }

   @Override
   public String toString() {
      String output = super.toString() + "\n" + "Identifier: " + _id;
      for (ExtendedAccessListLine line : _lines) {
         output += "\n" + line;
      }
      return output;
   }
}
