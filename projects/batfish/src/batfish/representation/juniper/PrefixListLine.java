package batfish.representation.juniper;

public class PrefixListLine {

   private String _prefix;
   private int _prefixLength;
   
   /* ------------------------------ Constructor ----------------------------*/
   public PrefixListLine(String ipmask) {
      String[] split_prefix = ipmask.split("/");
      _prefix = split_prefix[0];
      _prefixLength = Integer.parseInt(split_prefix[1]);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_prefix() {
      return _prefix;
   }
   public int get_prefixLength() {
      return _prefixLength;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/  
}
