package batfish.grammar.juniper.bgp;

import java.util.List;

public class FlatNeighborGBStanza extends GBStanza {
   private NGBType _type1;
	private String _neighborIP;
	private int _peerAS;
	private List<String> _exportNames;
	private int _localAS;
   private List<String> _importNames;

	public FlatNeighborGBStanza(String ip) {
		_neighborIP = ip;
		_peerAS = 0;
		_localAS = -1;
	}

	public void processStanza(NGBStanza ngbs) {
	   _type1=ngbs.getType();
		switch (ngbs.getType()) {
		case EXPORT:
			ExportNGBStanza engbs = (ExportNGBStanza) ngbs;
			_exportNames = engbs.getExportListNames();
			break;

		case PEER_AS:
			PeerASNGBStanza pngbs = (PeerASNGBStanza) ngbs;
			_peerAS = pngbs.getPeerASNum();
			break;

		case NULL:
			break;
			
      case IMPORT:
         ImportNGBStanza ingbs = (ImportNGBStanza) ngbs;
         _importNames = ingbs.getImportListNames();
         break;
         
      case LOCAL_AS:
         
         break;

		default:
			System.out.println("bad neighbor group bgp stanza type");
			break;
		}
	}

	public String getNeighborIP() {
		return _neighborIP;
	}

	public int getPeerAS() {
		return _peerAS;
	}

	public List<String> getExportNames() {
		return _exportNames;
	}

	public int getLocalAS() {
      return _localAS;
   }

   public List<String> getImportNames() {
      return _importNames;
   }
   
   public NGBType getType1(){
      return _type1;
   }
   
	@Override
	public GBType getType() {
		return GBType.NEIGHBOR;
	}

}
