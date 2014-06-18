package batfish.grammar.juniper.bgp;

import java.util.ArrayList;
import java.util.List;

public class ImportNGBStanza extends NGBStanza {
   private List<String> _importNames;

   public ImportNGBStanza() {
      _importNames = new ArrayList<String>();
   }
   
   public void addPS(String ps){
      _importNames.add(ps);
   }

   public List<String> getImportListNames() {
      return _importNames;
   }

   @Override
   public NGBType getType() {
      return NGBType.IMPORT;
   }

}
