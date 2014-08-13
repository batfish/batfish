package batfish.grammar.juniper;

public class NullJStanza extends JStanza {
   
   public NullJStanza (String ign) {
      this.set_stanzaStatus(StanzaStatusType.IGNORED);
      this.addIgnoredStatement(ign);
   }

	@Override
	public JStanzaType getType() {
		return JStanzaType.NULL;
	}

}
