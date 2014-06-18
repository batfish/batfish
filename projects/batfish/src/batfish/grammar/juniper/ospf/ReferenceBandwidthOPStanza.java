package batfish.grammar.juniper.ospf;

public class ReferenceBandwidthOPStanza extends OPStanza {
	private double _referenceBandwidth;

	public ReferenceBandwidthOPStanza(double rb) {
		_referenceBandwidth = rb;
	}
	
	public double getReferenceBandwidth(){
		return _referenceBandwidth;
	}

	@Override
	public OPType getType() {
		return OPType.REFERENCE_BANDWIDTH;
	}

}
