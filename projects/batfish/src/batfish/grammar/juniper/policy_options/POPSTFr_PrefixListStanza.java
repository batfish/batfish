package batfish.grammar.juniper.policy_options;

public class POPSTFr_PrefixListStanza extends POPST_FromStanza {
   
	private String _listName;
   
   /* ------------------------------ Constructor ----------------------------*/
   public POPSTFr_PrefixListStanza(String l) {
      _listName = l;
      set_postProcessTitle("Prefix List " + _listName);
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public String get_listName() {
      return _listName;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public POPST_FromType getType() {
		return POPST_FromType.PREFIX_LIST;
	}

}
