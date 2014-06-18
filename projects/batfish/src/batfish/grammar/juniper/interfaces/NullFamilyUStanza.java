package batfish.grammar.juniper.interfaces;

public class NullFamilyUStanza extends FamilyUStanza {

	@Override
	public FamilyUType getType() {
		return FamilyUType.NULL;
	}

}
