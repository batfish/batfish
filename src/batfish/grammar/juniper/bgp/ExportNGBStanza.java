package batfish.grammar.juniper.bgp;

import java.util.ArrayList;
import java.util.List;

public class ExportNGBStanza extends NGBStanza {
   private List<String> _exportNames;

   public ExportNGBStanza() {
      _exportNames = new ArrayList<String>();
   }

   public void addPS(String ps) {
      _exportNames.add(ps);
   }

   public List<String> getExportListNames() {
      return _exportNames;
   }

   @Override
   public NGBType getType() {
      return NGBType.EXPORT;
   }

}
