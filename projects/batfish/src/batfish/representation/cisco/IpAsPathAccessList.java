package batfish.representation.cisco;

import java.util.ArrayList;
import java.util.List;

public class IpAsPathAccessList {
   private List<IpAsPathAccessListLine> _lines;
   private String _name;

   public IpAsPathAccessList(String name) {
      _name = name;
      _lines = new ArrayList<IpAsPathAccessListLine>();
   }

   public void addLine(IpAsPathAccessListLine line) {
      _lines.add(line);
   }

   public List<IpAsPathAccessListLine> getLines() {
      return _lines;
   }

   public String getName() {
      return _name;
   }
}
