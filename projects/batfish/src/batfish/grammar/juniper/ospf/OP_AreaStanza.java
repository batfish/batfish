package batfish.grammar.juniper.ospf;

import java.util.ArrayList;
import java.util.List;

public class OP_AreaStanza extends OPStanza {
   
   private int _areaId;
   private List<OP_ARStanza> _opArStanzas;
   
   private List<String> _interfaceList;
   
   /* ------------------------------ Constructor ----------------------------*/
   public OP_AreaStanza() {
      _opArStanzas = new ArrayList<OP_ARStanza>();
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addOPARStanza(OP_ARStanza a) {
      _opArStanzas.add(a);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public void set_areaId(int n) {
      _areaId = n;
   }
   public List<String> get_interfaceList () {
      return _interfaceList;
   }
   public int get_areaID () {
	   return _areaId;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/   
   public void postProcessStanza() {
      _interfaceList = new ArrayList<String>();
      
      for (OP_ARStanza aops : _opArStanzas) {
         
         switch (aops.getType()) {
         
         case INTERFACE:
            OPAR_InterfaceStanza is = (OPAR_InterfaceStanza) aops;
            _interfaceList.add(is.get_ifName());
            break;

         case NULL:
            break;

         default:
            throw new Error("bad area stanza type");
         }
         this.addIgnoredStatements(aops.get_ignoredStatements());
      }
   }
	
	











	@Override
	public OPType getType() {
		return OPType.AREA;
	}

}
