package batfish.representation.cisco;

public class BgpNetwork {
	private String _networkAddress;
	private String _subnetMask;

	public BgpNetwork(String networkAddress, String subnetMask) {
		_networkAddress = networkAddress;
		_subnetMask = subnetMask;
	}

	public String getNetworkAddress() {
		return _networkAddress;
	}

	public String getSubnetMask() {
		return _subnetMask;
	}

}
