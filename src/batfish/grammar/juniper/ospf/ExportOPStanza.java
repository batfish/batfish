package batfish.grammar.juniper.ospf;

import java.util.ArrayList;
import java.util.List;

public class ExportOPStanza extends OPStanza {
   private List<String> _exports;

   public ExportOPStanza() {
      _exports = new ArrayList<String>();
   }

   public void addPS(String e) {
      _exports.add(e);
   }

   public List<String> getExports() {
      return _exports;
   }

   @Override
   public OPType getType() {
      return OPType.EXPORT;
   }

}
