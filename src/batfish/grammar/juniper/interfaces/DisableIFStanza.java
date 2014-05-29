package batfish.grammar.juniper.interfaces;

public class DisableIFStanza extends IFStanza {

	@Override
	public IFType getType() {
		return IFType.DISABLE;
	}

}
