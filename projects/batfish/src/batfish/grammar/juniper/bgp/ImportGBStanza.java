package batfish.grammar.juniper.bgp;

import java.util.ArrayList;
import java.util.List;

public class ImportGBStanza extends GBStanza {
   private List<String> _importNames;

   public ImportGBStanza() {
      _importNames = new ArrayList<String>();
   }
   
   public void addPS(String ps){
      _importNames.add(ps);
   }

   public List<String> getImportListNames() {
      return _importNames;
   }

   @Override
   public GBType getType() {
      return GBType.IMPORT;
   }

}
