package batfish.grammar.juniper.interfaces;

public class IFUF_FilterStanza extends IFU_FamStanza {
   
   private String _inStr;
   private String _outStr;
   private boolean _inputInactive=false;
   private boolean _outputInactive=false;
   
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
   
   public void set_inputInactive (boolean b) {
      _inputInactive = b;
   }
   
   public void set_outputInactive (boolean b) {
      _outputInactive = b;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public void postProcessStanza () {
      if (_inputInactive) {
         addIgnoredStatement("inactive input " + _inStr);
      }
      if (_outputInactive) {
         addIgnoredStatement("inactive output " + _inStr);
      }
      set_alreadyAggregated(false);
      set_postProcessTitle("filter");
      super.postProcessStanza();
   }
   
   @Override
   public IFU_FamType getType() {
      return IFU_FamType.FILTER;
   }
}
