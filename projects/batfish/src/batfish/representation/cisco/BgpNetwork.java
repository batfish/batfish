package batfish.representation.cisco;

import batfish.representation.Ip;

public class BgpNetwork {
	private Ip _networkAddress;
	private Ip _subnetMask;

	public BgpNetwork(Ip network, Ip subnet) {
		_networkAddress = network;
		_subnetMask = subnet;
	}

	public Ip getNetworkAddress() {
		return _networkAddress;
	}

   public Ip getSubnetMask() {
		return _subnetMask;
	}

}
