package batfish.grammar.juniper.system;

import batfish.grammar.juniper.StanzaStatusType;

public class Sys_NullStanza extends SysStanza {
   
   /* ------------------------------ Constructor ----------------------------*/
   public Sys_NullStanza (String ign) {
      set_stanzaStatus(StanzaStatusType.IGNORED);
      addIgnoredStatement(ign);
      set_postProcessTitle("[ignored statement]");
   }

   /* ----------------------------- Other Methods ---------------------------*/
   
   /* ---------------------------- Getters/Setters --------------------------*/
   
   /* --------------------------- Inherited Methods -------------------------*/
	@Override
	public SysStanzaType getSysType() {
		return SysStanzaType.NULL;
	}

}
