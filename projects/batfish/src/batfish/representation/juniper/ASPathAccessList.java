package batfish.representation.juniper;

import java.io.Serializable;

public class ASPathAccessList implements Serializable {

   private static final long serialVersionUID = 1L;

   private String _name;
   private String _regex;
   
   /* ------------------------------ Constructor ----------------------------*/
   public ASPathAccessList(String n, String r) {
      _name = n;
      _regex = r;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_name() {
      return _name;
   }   
   public String get_regex() {
      return _regex;
   }
   
   
   /* --------------------------- Inherited Methods -------------------------*/  
}
