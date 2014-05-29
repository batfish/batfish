package batfish.grammar.juniper.bgp;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.PStanza;
import batfish.grammar.juniper.PType;
import batfish.representation.juniper.BGPGroup;

public class FlatBGPPStanza extends PStanza {
   private BPType _type1;
   private GBType _type2;
   private NGBType _type3;
	private List<BGPGroup> _groupList;
	private List<String> _activatedNeighbor;

	public FlatBGPPStanza() {
		_groupList = new ArrayList<BGPGroup>();
		_activatedNeighbor = new ArrayList<String>();
	}

	public void processStanza(BPStanza bps) {
	   _type1 = bps.getType();
		switch (bps.getType()) {
		case GROUP:		   
			FlatGroupBPStanza gbps = (FlatGroupBPStanza) bps;
			_type2 = gbps.getType1();
			_type3 = gbps.getType2();
			if (gbps.isIPV4()) {
				_groupList.add(gbps.getGroup());
				_activatedNeighbor.addAll(gbps.getActivatedNeighbor());
			}
			break;

		case NULL:
			break;

		default:
			System.out.println("bad bgp stanza type");
			break;
		}
	}

	public List<BGPGroup> getGroupList() {
		return _groupList;
	}

	public List<String> getActivatedNeighbor() {
		return _activatedNeighbor;
	}
	
	public BPType getType1(){
	   return _type1;
	}
	
	public GBType getType2(){
      return _type2;
   }
   
   public NGBType getType3(){
      return _type3;
   }

	@Override
	public PType getType() {
		return PType.BGP;
	}

}
