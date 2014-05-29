package batfish.grammar.juniper.interfaces;

import batfish.representation.SwitchportMode;

public class InterfaceModeFamilyUStanza extends FamilyUStanza {
	private SwitchportMode _mode;

	public InterfaceModeFamilyUStanza(SwitchportMode m) {
		_mode = m;
	}

	public SwitchportMode getMode() {
		return _mode;
	}

	@Override
	public FamilyUType getType() {
		return FamilyUType.INTERFACE_MODE;
	}

}
