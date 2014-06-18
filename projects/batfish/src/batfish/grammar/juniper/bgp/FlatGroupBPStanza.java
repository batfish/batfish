package batfish.grammar.juniper.bgp;

import java.util.ArrayList;
import java.util.List;

import batfish.representation.juniper.BGPGroup;
import batfish.representation.juniper.BGPNeighbor;
import batfish.util.Util;

public class FlatGroupBPStanza extends BPStanza {
   private GBType _type1;
   private NGBType _type2;
   private List<String> _activatedNeighbor;
   private List<BGPNeighbor> _neighbors;
   private String _groupName;
   private int _localAS;
   private boolean _isIPV4;
   private boolean _isExternal;
   private BGPGroup _group;

   public FlatGroupBPStanza(String n) {
      _groupName = n;
      _neighbors = new ArrayList<BGPNeighbor>();
      _activatedNeighbor = new ArrayList<String>();
      _isIPV4 = true;
      _isExternal = false;
      _group = new BGPGroup(_groupName);
   }

   public void processStanza(GBStanza gbs) {
      if (_isIPV4) {
         _type1 = gbs.getType();
         switch (gbs.getType()) {
         case CLUSTER:
            ClusterGBStanza cgbs = (ClusterGBStanza) gbs;
            if (_isExternal) {
               System.out.println("setting route reflector on external group ");
            }
            else {
               _group.setClusterId(Util.ipToLong(cgbs.getIP()));
               _group.setRouteReflectorClient();
            }

            break;

         case FAMILY:
            FamilyGBStanza fgbs = (FamilyGBStanza) gbs;
            _isIPV4 = fgbs.IsIPv4Family();
            if (!(_isIPV4)) {
               _group = null;
            }
            break;

         case LOCAL_AS:
            LocalASGBStanza lgbs = (LocalASGBStanza) gbs;
            _localAS = lgbs.getLocalASNum();
            _group.setLocalAS(_localAS);
            break;

         case NEIGHBOR:
            FlatNeighborGBStanza ngbs = (FlatNeighborGBStanza) gbs;
            _type2 = ngbs.getType1();
            _activatedNeighbor.add(ngbs.getNeighborIP());
            BGPNeighbor tmp = new BGPNeighbor(ngbs.getNeighborIP());
            tmp.setInboundPolicyStatement(ngbs.getImportNames());
            tmp.setRemoteAS(ngbs.getPeerAS());
            tmp.setOutboundPolicyStatement(ngbs.getExportNames());
            tmp.setLocalAS(ngbs.getLocalAS());
            _group.addNeighbor(tmp);
            break;

         case NULL:
            break;

         case TYPE:
            TypeGBStanza tgbs = (TypeGBStanza) gbs;
            _isExternal = tgbs.isExternal();
            break;
            
         case EXPORT:
            ExportGBStanza egbs = (ExportGBStanza) gbs;
            _group.setOutboundPolicyStatement(egbs.getExportListNames());
            break;
         case IMPORT:
            throw new Error("not implemented");
         case PEER_AS:
         case LOCAL_ADDRESS:
            throw new Error("not implemented");

         default:
            System.out.println("bad group bgp stanza type");
            break;
         }
      }
   }

   public BGPGroup getGroup() {
      if (_isIPV4) {
         return _group;
      }
      else {
         return null;
      }
   }

   public List<BGPNeighbor> getNeighbors() {
      if (_isIPV4) {
         return _neighbors;
      }
      else {
         return null;
      }
   }

   public List<String> getActivatedNeighbor() {
      if (_isIPV4) {
         return _activatedNeighbor;
      }
      else {
         return null;
      }
   }

   public int getLocalAS() {
      return _localAS;
   }

   public boolean isIPV4() {
      return _isIPV4;
   }

   public boolean isExternal() {
      return _isExternal;
   }
   
   public GBType getType1(){
      return _type1;
   }
   
   public NGBType getType2(){
      return _type2;
   }

   @Override
   public BPType getType() {
      return BPType.GROUP;
   }

}
