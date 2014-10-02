package batfish.grammar.juniper.protocols;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import batfish.grammar.juniper.JStanza;
import batfish.grammar.juniper.JStanzaType;
import batfish.grammar.juniper.bgp.BGPStanza;
import batfish.grammar.juniper.ospf.OSPFStanza;
import batfish.representation.juniper.BGPGroup;

public class ProtocolsStanza extends JStanza {
   
   private List<PStanza> _pStanzas;
   
   private HashMap<Integer, List<String>>  _ospfAreaMap;
   private List<String> _ospfExports;
   private double _ospfReferenceBandwidth;
   

   private List<BGPGroup> _groupList;
   private List<String> _activatedNeighbors;
      
   /* ------------------------------ Constructor ----------------------------*/
   public ProtocolsStanza() {
      _pStanzas = new ArrayList<PStanza> ();
   }
   /* ----------------------------- Other Methods ---------------------------*/
   public void addPStanza (PStanza p) {
      _pStanzas.add(p);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public double get_ospfReferenceBandwidth() {
      return _ospfReferenceBandwidth;
   }
   public HashMap<Integer, List<String>> get_ospfAreaMap() {
      return _ospfAreaMap;
   }
   public List<String> get_ospfExports() {
      return _ospfExports;
   }
   public List<BGPGroup> get_groupList() {
      return _groupList;
   }

   public List<String> get_activatedNeighbors() {
      return _activatedNeighbors;
   }

   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public void postProcessStanza() {
      super.postProcessStanza();
      
      for (PStanza ps : _pStanzas) {
         ps.postProcessStanza();
         switch (ps.getType()) {
         case BGP:
            BGPStanza bps = (BGPStanza) ps;
            _groupList = bps.get_groupList();
            _activatedNeighbors = bps.get_activatedNeighbors();
            break;
   
         case OSPF:
            OSPFStanza ops = (OSPFStanza) ps;
            _ospfAreaMap = ops.get_areaMap();
            _ospfReferenceBandwidth = ops.get_referenceBandwidth();
            _ospfExports = ops.get_exportPolicies();
            break;
   
         case NULL:
            break;
   
         default:
             throw new Error ("bad protocols stanza type");
         }

         this.addIgnoredStatements(ps.get_ignoredStatements());
      }
   }

   @Override
   public JStanzaType getType() {
      return JStanzaType.PROTOCOLS;
   }

}
