package batfish.grammar.juniper.interfaces;

public class IFUF_FilterStanza extends IFU_FamStanza {
   
   private String _inStr;
   private String _outStr;
   
   /* ------------------------------ Constructor ----------------------------*/
   public IFUF_FilterStanza () {
      _inStr = "";
      _outStr = "";
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public void set_inStr (String s) {
      _inStr = s;
   }
   
   public void set_outStr (String s) {
      _outStr = s;
   }
   
   public String get_inStr() {
      return _inStr;
   }
   
   public String get_outStr() {
      return _outStr;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public IFU_FamType getType() {
      return IFU_FamType.FILTER;
   }
}
