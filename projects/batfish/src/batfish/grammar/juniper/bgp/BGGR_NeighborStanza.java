package batfish.grammar.juniper.bgp;

import static batfish.representation.juniper.FamilyOps.FamilyTypeToString;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.StanzaStatusType;
import batfish.representation.juniper.FamilyType;

public class BGGR_NeighborStanza extends BG_GRStanza {
   
	private String _neighborIP;
   private List<String> _exportNames;
   private List<String> _importNames;
   private int _peerAS;
	private String _localAddress;
   
   private List<BGGR_NStanza> _bggrnStanzas;
   
   /* ------------------------------ Constructor ----------------------------*/
   public BGGR_NeighborStanza() {
      _bggrnStanzas = new ArrayList<BGGR_NStanza> ();
      set_postProcessTitle("Neighbor");
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addBGGRNStanza (BGGR_NStanza bggn) {
      _bggrnStanzas.add(bggn);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public void set_neighborIP (String ip) {
      _neighborIP = ip;
      set_postProcessTitle("Neighbor " + _neighborIP);
   }
   public String get_neighborIP () {
      return _neighborIP;
   }
   public List<String> get_importNames () {
      return _importNames;
   }
   public List<String> get_exportNames () {
      return _exportNames;
   }
   public int get_peerAS () {
      return _peerAS;
   }
   public String get_localAddress () {
      return _localAddress;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/  
   @Override
   public void postProcessStanza() {
      
      _exportNames = new ArrayList<String>();
      _importNames = new ArrayList<String>();
      _peerAS = 0;
      _localAddress = "";
      
      for (BGGR_NStanza bggrn : _bggrnStanzas) {
         
         bggrn.postProcessStanza();
         
         if (bggrn.get_stanzaStatus() == StanzaStatusType.ACTIVE) {
            switch (bggrn.getType()) {
            
            case EXPORT:
               BGGRN_ExportStanza engbs = (BGGRN_ExportStanza) bggrn;
               _exportNames.addAll(engbs.GetExportNames());
               break;
               
            case FAMILY:
               BGGRN_FamilyStanza fngbs = (BGGRN_FamilyStanza) bggrn;
               // TODO [Ask Ari]: What to do with family in neighbor
               break;
               
            case IMPORT:
               BGGRN_ImportStanza ingbs = (BGGRN_ImportStanza) bggrn;
               _importNames.addAll(ingbs.GetImportNames());
               break;
               
            case LOCAL_ADDRESS:
               BGGRN_LocalAddressStanza lngbs = (BGGRN_LocalAddressStanza) bggrn;
               _localAddress = lngbs.GetLocalAddress();
               if (lngbs.get_stanzaStatus() == StanzaStatusType.IPV6) {
                  this.set_stanzaStatus(StanzaStatusType.IPV6);
               }
               break;
   
            case PEER_AS:
               BGGRN_PeerAsStanza pngbs = (BGGRN_PeerAsStanza) bggrn;
               _peerAS = pngbs.GetASNum();
               break;
   
            case NULL:
               break;
   
            default:
               throw new Error("bad neighbor group bgp stanza type");
            }
         }
         this.addIgnoredStatements(bggrn.get_ignoredStatements());
      }
      if (get_stanzaStatus()==StanzaStatusType.IPV6) {       
         clearIgnoredStatements();
         addIgnoredStatement("Neighbor " + _neighborIP + "{...}");
         set_alreadyAggregated(true);
      }
      else {
         set_alreadyAggregated(false);
      }
      super.postProcessStanza();
   }
   
	@Override
	public BG_GRType getType() {
		return BG_GRType.NEIGHBOR;
	}

}
