package batfish.representation.juniper;

public class Martian {

   private String _ipWithMask;
   private FilterMatch _fm;
   private boolean _isAllowed;
   private boolean _isIPV6;
   
   /* ------------------------------ Constructor ----------------------------*/
   public Martian() {
      _ipWithMask = "";
      _isAllowed = false;
      _isIPV6 = false;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/ 
   public void set_ipWithMask (String i) {
      _ipWithMask = i;      
   }
   public void set_fm (FilterMatch f) {
      _fm = f;   
   }
   public void set_isAllowed (boolean i) {
      _isAllowed = i;
   }
   public void set_isIPV6 (boolean i) {
      _isIPV6 = i;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/  
}
