package batfish.grammar.juniper.bgp;

public class PeerASNGBStanza extends NGBStanza {
	private int _peerASNum;

	public PeerASNGBStanza(int a) {
		_peerASNum = a;
	}

	public int getPeerASNum() {
		return _peerASNum;
	}

	@Override
	public NGBType getType() {
		return NGBType.PEER_AS;
	}

}
