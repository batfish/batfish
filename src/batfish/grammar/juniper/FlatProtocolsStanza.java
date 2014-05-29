package batfish.grammar.juniper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import batfish.grammar.juniper.bgp.BPType;
import batfish.grammar.juniper.bgp.FlatBGPPStanza;
import batfish.grammar.juniper.bgp.GBType;
import batfish.grammar.juniper.bgp.NGBType;
import batfish.grammar.juniper.ospf.AOPType;
import batfish.grammar.juniper.ospf.FlatOSPFPStanza;
import batfish.grammar.juniper.ospf.OPType;
import batfish.representation.juniper.BGPGroup;

public class FlatProtocolsStanza extends JStanza {
   private PType _type1;
   private OPType _oType2;
   private AOPType _oType3;
   private BPType _bType2;
   private GBType _bType3;
   private NGBType _bType4;
   private HashMap<Integer, ArrayList<String>> _ospfAreaMap;
   private List<BGPGroup> _groupList;
   private List<String> _activatedNeighbor;
   private List<String> _ospfExports;
   private double _referenceBandwidth;

   public void processStanza(PStanza ps) {
      _type1=ps.getType();
      switch (ps.getType()) {
      case BGP:
         FlatBGPPStanza bps = (FlatBGPPStanza) ps;
         _bType2 = bps.getType1();
         _bType3 = bps.getType2();
         _bType4 = bps.getType3();
         _groupList = bps.getGroupList();
         _activatedNeighbor = bps.getActivatedNeighbor();
         break;

      case NULL:
         break;

      case OSPF:         
         FlatOSPFPStanza ops = (FlatOSPFPStanza) ps;
         _oType2 = ops.getType1();
         _oType3 = ops.getType2();
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
   
   public PType getType1(){
      return _type1;
   }
   
   public OPType getOType2(){
      return _oType2;
   }
   
   public AOPType getOType3(){
      return _oType3;
   }
   
   public BPType getBType2(){
      return _bType2;
   }
   
   public GBType getBType3(){
      return _bType3;
   }
   
   public NGBType getBType4(){
      return _bType4;
   }

   @Override
   public JStanzaType getType() {
      return JStanzaType.PROTOCOLS;
   }

}
