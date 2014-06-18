package batfish.grammar.juniper.bgp;

import java.util.ArrayList;
import java.util.List;

public class ExportGBStanza extends GBStanza {
   private List<String> _exportNames;

   public ExportGBStanza() {
      _exportNames = new ArrayList<String>();
   }

   public void addPS(String ps) {
      _exportNames.add(ps);
   }

   public List<String> getExportListNames() {
      return _exportNames;
   }


   @Override
   public GBType getType() {
      return GBType.EXPORT;
   }

}
