package batfish.grammar.juniper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import batfish.grammar.juniper.bgp.BGPPStanza;
import batfish.grammar.juniper.ospf.OSPFPStanza;
import batfish.representation.juniper.BGPGroup;

public class ProtocolsStanza extends JStanza {
   private HashMap<Integer, ArrayList<String>> _ospfAreaMap;
   private List<BGPGroup> _groupList;
   private List<String> _activatedNeighbor;
   private List<String> _ospfExports;
   private double _referenceBandwidth;

   public void processStanza(PStanza ps) {
      switch (ps.getType()) {
      case BGP:
         BGPPStanza bps = (BGPPStanza) ps;
         _groupList = bps.getGroupList();
         _activatedNeighbor = bps.getActivatedNeighbor();
         break;

      case NULL:
         break;

      case OSPF:
         OSPFPStanza ops = (OSPFPStanza) ps;
         _ospfAreaMap = ops.getAreaMap();
         _referenceBandwidth = ops.getReferenceBandwidth();
         _ospfExports = ops.getExports();
         break;

      default:
         System.out.println("bad protocols stanza type");
         break;
      }
   }

   public HashMap<Integer, ArrayList<String>> getOSPFAreaMap() {
      return _ospfAreaMap;
   }

   public List<BGPGroup> getGroupList() {
      return _groupList;
   }

   public List<String> getActivatedNeighbor() {
      return _activatedNeighbor;
   }

   public double getReferenceBandwidth() {
      return _referenceBandwidth;
   }
   
   public List<String> getOSPFExports(){
      return _ospfExports;
   }

   @Override
   public JStanzaType getType() {
      return JStanzaType.PROTOCOLS;
   }

}
