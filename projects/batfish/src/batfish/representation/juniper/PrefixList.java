package batfish.representation.juniper;

import java.util.List;

public class PrefixList {

   private String _name;
   private List<PrefixListLine> _prefixes;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PrefixList(String n, List<PrefixListLine> ps) {
      _name = n;
      _prefixes = ps;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public List<PrefixListLine> get_prefixes() {
      return _prefixes;
   }
   public String get_name() {
      return _name;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/  
}
