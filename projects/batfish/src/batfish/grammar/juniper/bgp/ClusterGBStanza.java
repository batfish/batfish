package batfish.grammar.juniper.bgp;

public class ClusterGBStanza extends GBStanza {
	private String _ip;

	public ClusterGBStanza(String i) {
		_ip = i;
	}
	
	public String getIP(){
		return _ip;
	}

	@Override
	public GBType getType() {
		return GBType.CLUSTER;
	}

}
