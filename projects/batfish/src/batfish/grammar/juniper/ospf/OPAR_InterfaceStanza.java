package batfish.grammar.juniper.ospf;

public class OPAR_InterfaceStanza extends OP_ARStanza {
   
	private String _ifName;
	
   /* ------------------------------ Constructor ----------------------------*/
   public OPAR_InterfaceStanza(String n) {
      _ifName = n;
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_ifName() {
      return _ifName;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/  
	@Override
	public OP_ARType getType() {
		return OP_ARType.INTERFACE;
	}

}
