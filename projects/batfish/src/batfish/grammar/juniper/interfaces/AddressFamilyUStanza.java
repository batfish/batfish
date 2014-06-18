package batfish.grammar.juniper.interfaces;

public class AddressFamilyUStanza extends FamilyUStanza {
	private String _address;
	private String _subnetMask;

	public AddressFamilyUStanza(String a) {
		String[] temp = a.split("/");
		_address = temp[0];
		_subnetMask = temp[1];
	}

	public String getAddress() {
		return _address;
	}

	public String getSubnetMask() {
		return _subnetMask;
	}

	@Override
	public FamilyUType getType() {
		return FamilyUType.ADDRESS;
	}

}
