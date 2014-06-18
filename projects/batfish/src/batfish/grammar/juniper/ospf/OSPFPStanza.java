package batfish.grammar.juniper.ospf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import batfish.grammar.juniper.PStanza;
import batfish.grammar.juniper.PType;

public class OSPFPStanza extends PStanza {
   private HashMap<Integer, ArrayList<String>> _areaMap;
   private double _referenceBandwidth;
   private List<String> _exports;

   public OSPFPStanza() {
      _areaMap = new HashMap<Integer, ArrayList<String>>();
      _referenceBandwidth = -1.0;
      _exports = new ArrayList<String>();
   }

   public void processStanza(OPStanza ops) {
      switch (ops.getType()) {
      case AREA:
         AreaOPStanza aops = (AreaOPStanza) ops;
         ArrayList<String> iflist = aops.getInterfaceList();
         _areaMap.put(aops.getAreaNum(), iflist);
         break;

      case NULL:
         break;

      case REFERENCE_BANDWIDTH:
         ReferenceBandwidthOPStanza rbops = (ReferenceBandwidthOPStanza) ops;
         _referenceBandwidth = rbops.getReferenceBandwidth();
         break;

      case EXPORT:
         ExportOPStanza eops = (ExportOPStanza) ops;
         _exports.addAll(eops.getExports());
         break;

      default:
         System.out.println("bad ospf stanza type");
         break;
      }
   }

   public HashMap<Integer, ArrayList<String>> getAreaMap() {
      return _areaMap;
   }

   public double getReferenceBandwidth() {
      return _referenceBandwidth;
   }
   
   public List<String> getExports(){
      return _exports;
   }

   @Override
   public PType getType() {
      return PType.OSPF;
   }

}
