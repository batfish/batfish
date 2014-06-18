package batfish.grammar.juniper.bgp;

import java.util.ArrayList;
import java.util.List;

public class NeighborGBStanza extends GBStanza {
	private String _neighborIP;
	private int _peerAS;
	private List<String> _exportNames;
	private int _localAS;
   private List<String> _importNames;

	public NeighborGBStanza(String ip) {
		_neighborIP = ip;
		_peerAS = 0;
		_localAS = -1;
		_importNames = new ArrayList<String>();
		_exportNames = new ArrayList<String>();
	}

	public void processStanza(NGBStanza ngbs) {
		switch (ngbs.getType()) {
		case EXPORT:
			ExportNGBStanza engbs = (ExportNGBStanza) ngbs;
			_exportNames.addAll(engbs.getExportListNames());
			break;

		case PEER_AS:
			PeerASNGBStanza pngbs = (PeerASNGBStanza) ngbs;
			_peerAS = pngbs.getPeerASNum();
			break;

		case NULL:
			break;
			
      case IMPORT:
         ImportNGBStanza ingbs = (ImportNGBStanza) ngbs;
         _importNames.addAll(ingbs.getImportListNames());
         break;
         
      case LOCAL_AS:
         LocalASNGBStanza angbs = (LocalASNGBStanza) ngbs;
         _localAS = angbs.getLocalASNum();         
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
   
	@Override
	public GBType getType() {
		return GBType.NEIGHBOR;
	}

}
