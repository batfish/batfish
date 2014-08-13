package batfish.grammar.juniper.ospf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import batfish.grammar.juniper.protocols.PStanza;
import batfish.grammar.juniper.protocols.PType;

public class OSPFStanza extends PStanza {
   
   private List<OPStanza> _opStanzas;
   
   private double _referenceBandwidth;
   private List<String> _exportPolicies;
   private HashMap<Integer, List<String>> _areaMap;  
   
   /* ------------------------------ Constructor ----------------------------*/
   public OSPFStanza() {
      _opStanzas = new ArrayList<OPStanza>();
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addOPStanza (OPStanza o) {
      _opStanzas.add(o);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public HashMap<Integer, List<String>> get_areaMap (){
      return _areaMap;
   }
   public double get_referenceBandwidth () {
      return _referenceBandwidth;
   }
   public List<String> get_exportPolicies () {
      return _exportPolicies;
   }
     
   /* --------------------------- Inherited Methods -------------------------*/ 
   public void processStanza() {
      
      _referenceBandwidth = -1.0;              // TODO [P1]: get rid of magic constant
      _exportPolicies = new ArrayList<String>();
      _areaMap = new HashMap<Integer, List<String>>();
      
      for (OPStanza ops : _opStanzas) { 
         ops.postProcessStanza();
         
         switch (ops.getType()) {
         case AREA:
            OP_AreaStanza aops = (OP_AreaStanza) ops;
            _areaMap.put(aops.get_areaID(), aops.get_interfaceList());
            break;
   
         case REFERENCE_BANDWIDTH:
            OP_ReferenceBandwidthStanza rbops = (OP_ReferenceBandwidthStanza) ops;
            _referenceBandwidth = rbops.getReferenceBandwidth();
            break;
   
         case EXPORT:
            OP_ExportStanza eops = (OP_ExportStanza) ops;
            _exportPolicies.addAll(eops.get_policyNames());
            break;
   
         case NULL:
            break;
   
         default:
            throw new Error("bad ospf stanza type");
         }
         this.addIgnoredStatements(ops.get_ignoredStatements());
      }
   }

   @Override
   public PType getType() {
      return PType.OSPF;
   }

}
