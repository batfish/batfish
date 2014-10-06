package batfish.grammar.juniper.bgp;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.StanzaStatusType;
import batfish.representation.juniper.BGPGroup;
import batfish.representation.juniper.BGPNeighbor;

public class BG_GroupStanza extends BGStanza {

   private String _groupName;
   private BGPGroup _group;
   private List<String> _activatedNeighbors;
   
   private String _localAddress;
   private int _localAS;
   private int _peerAS;
   private boolean _isExternal;
   
   private List<BG_GRStanza> _bggrStanzas;
  

   
   /* ------------------------------ Constructor ----------------------------*/
   public BG_GroupStanza(String n) {
      _groupName = n;
      _bggrStanzas = new ArrayList<BG_GRStanza>();
      set_postProcessTitle("BGP Group " +n);
   }

   /* ----------------------------- Other Methods ---------------------------*/
   public void AddBGGRStanza (BG_GRStanza bggrs) {
      _bggrStanzas.add(bggrs);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public BGPGroup get_group () {
      return _group;
   }
   public List<String> get_activatedNeighbors () {
      return _activatedNeighbors;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/  
   @Override
   public void postProcessStanza() {

      _group = new BGPGroup(_groupName);
      _activatedNeighbors = new ArrayList<String>();
      _localAddress = "";
      _localAS = 0;
      _peerAS = 0; 
      _isExternal = false;
      
      for (BG_GRStanza bggrs : _bggrStanzas) {
         
         bggrs.postProcessStanza();
         
         if (bggrs.get_stanzaStatus()==StanzaStatusType.ACTIVE) {
         
            switch (bggrs.getType()) {
            
            case EXPORT:
               BGGR_ExportStanza egbs = (BGGR_ExportStanza) bggrs;
               _group.setOutboundPolicyStatement(egbs.GetExportNames());
               break;
               
            case FAMILY:
               BGGR_FamilyStanza fgbs = (BGGR_FamilyStanza) bggrs;
               // TODO [Ask Ari]: what to do with family inside group
               break;
               
            case IMPORT:
               BGGR_ImportStanza igbs = (BGGR_ImportStanza) bggrs;
               _group.setInboundPolicyStatement(igbs.GetImportNames());
               break;
            
            case LOCAL_ADDRESS:
               BGGR_LocalAddressStanza lagbs = (BGGR_LocalAddressStanza) bggrs;
               _localAddress = lagbs.GetLocalAddress();
               _group.setUpdateSource(_localAddress);
               if (lagbs.get_stanzaStatus() == StanzaStatusType.IPV6) {
                  this.set_stanzaStatus(StanzaStatusType.IPV6);
               }
               break;
               
            case LOCAL_AS:
               BGGR_LocalAsStanza lasgbs = (BGGR_LocalAsStanza) bggrs;
               _localAS = lasgbs.get_localASNum();
               _group.setLocalAS(_localAS);
               break;
               
            case NEIGHBOR:
               BGGR_NeighborStanza ngbs = (BGGR_NeighborStanza) bggrs;
               
               _activatedNeighbors.add(ngbs.get_neighborIP());
               BGPNeighbor bgpNeighbor = new BGPNeighbor(ngbs.get_neighborIP());
               bgpNeighbor.setInboundPolicyStatement(ngbs.get_importNames());
               bgpNeighbor.setOutboundPolicyStatement(ngbs.get_exportNames());
               bgpNeighbor.setRemoteAS(ngbs.get_peerAS());
               bgpNeighbor.setLocalAddress(ngbs.get_localAddress());
               _group.addNeighbor(bgpNeighbor);
               break;
               
            case PEER_AS:
               BGGR_PeerAsStanza pgbs = (BGGR_PeerAsStanza) bggrs;
               _peerAS = pgbs.GetASNum();
               _group.setRemoteAS(_peerAS);
               break;
               
            case TYPE:
               BGGR_TypeStanza tgbs = (BGGR_TypeStanza) bggrs;
               _isExternal = tgbs.get_isExternal();
               _group.setIsExternal(_isExternal);
               break;
               
            case NULL:
               break;
               
            default:
               throw new Error("bad group bgp stanza type");
            }
         }
         this.addIgnoredStatements(bggrs.get_ignoredStatements());
      
         /*case CLUSTER: // TODO [Ask Ari]: shouldn't be ignored
            ClusterGBPStanza cgbs = (ClusterGBPStanza) gbs;
            if (_isExternal) {
               throw new Error("setting route reflector on external group ");
            }
            else {
               _group.setClusterId(Util.ipToLong(cgbs.getIP()));
               _group.setRouteReflectorClient();
            }

            break;
         */
      }

      if (get_stanzaStatus()==StanzaStatusType.IPV6) {       
         clearIgnoredStatements();
         addIgnoredStatement("group " + _groupName + "{...}");
         set_alreadyAggregated(true);
      }
      else {
         set_alreadyAggregated(false);
      }
      super.postProcessStanza();
   }


   @Override
   public BGType getType() {
      return BGType.GROUP;
   }

}
