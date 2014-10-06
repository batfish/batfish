package batfish.grammar.juniper.interfaces;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.StanzaStatusType;
import batfish.grammar.juniper.StanzaWithStatus;
import batfish.representation.juniper.Interface;

public class InterfaceStanza extends StanzaWithStatus {
   
   private String _name;
   private List<IFStanza> _ifStanzas;
   private List<Interface> _interfaces;
   
   /* ------------------------------ Constructor ----------------------------*/
   public InterfaceStanza() {
      _name = "";
      _ifStanzas = new ArrayList<IFStanza>();
      _interfaces = new ArrayList<Interface>();
      this.set_postProcessTitle("Interface (anon)");
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addIFStanza (IFStanza ifs) {
      _ifStanzas.add(ifs);
   }
   private Double getDefaultBandwidth(String name) {
      Double bandwidth = null;
      if ((name.startsWith("fe")) || (name.startsWith("em"))) {
         bandwidth = 100E6;
      }
      else if (name.startsWith("lo")) {
         bandwidth = 1E12;
      }
      else if (name.startsWith("ge")) {
         bandwidth = 1E9;
      }
      else if (name.startsWith("xe")) {
         bandwidth = 10E9;
      }
      if (bandwidth == null) {
         bandwidth = 1.0;
      }
      return bandwidth;
   }
   /* ---------------------------- Getters/Setters --------------------------*/
   public void set_name (String n) {
      _name = n;
      this.set_postProcessTitle("Interface " + _name);
   }
   
   public List<Interface> get_interfaces (){
      return _interfaces;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public void postProcessStanza() {
      
      for (IFStanza ifs : _ifStanzas) {                         // process each separate sub-stanza 
         
         ifs.postProcessStanza();

         if (ifs.get_stanzaStatus() == StanzaStatusType.ACTIVE) {
         
            switch (ifs.getType()) {
               case DISABLE:
                  Interface i = new Interface(_name);
                  i.set_active(false);
                  i.set_bandwidth(getDefaultBandwidth(_name));      // TODO [P1]: shouldn't this default happen in Interface?
                  _interfaces.add(i);
                  break;
                  
               case UNIT:                                             
                  IF_UnitStanza ifus = (IF_UnitStanza) ifs;
                  String u = Integer.toString(ifus.get_num());
                  Interface ui = new Interface(_name + "." + u);
                  
                  if (!ifus.get_address().isEmpty() && ifus.get_subnetMask() !=null) { 
                     ui.set_ip(ifus.get_address());
                     ui.set_subnet(ifus.get_subnetMask());
                     ui.set_bandwidth(getDefaultBandwidth(_name));
                     _interfaces.add(ui);
                  }
                  break;
                  
               case APPLY_GROUPS:                                  // TODO [P0]: figure this out!
                  break;
                  
               case NULL:
                  break;
                  
            }
         }
         addIgnoredStatements(ifs.get_ignoredStatements());
      }
      set_alreadyAggregated(false);
      super.postProcessStanza();
   }
}
         
