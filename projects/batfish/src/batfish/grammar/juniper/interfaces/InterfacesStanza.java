package batfish.grammar.juniper.interfaces;

import java.util.ArrayList;
import java.util.List;

import batfish.grammar.juniper.JStanza;
import batfish.grammar.juniper.JStanzaType;
import batfish.grammar.juniper.StanzaStatusType;
import batfish.representation.juniper.Interface;

public class InterfacesStanza extends JStanza {
   
	private List<InterfaceStanza> _interfaceStanzas;  
   private List<Interface> _interfaces;         
   
   /* ------------------------------ Constructor ----------------------------*/
   public InterfacesStanza () {
      _interfaceStanzas = new ArrayList<InterfaceStanza>();
      _interfaces = new ArrayList<Interface>();
      set_postProcessTitle("Interfaces");
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void addInterfaceStanza (InterfaceStanza is) {
      _interfaceStanzas.add(is);
   }
   
   /* ---------------------------- Getters/Setters --------------------------*/
   public List<Interface> get_interfaces (){
      return _interfaces;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/  
   @Override
   public void postProcessStanza() {
      for (InterfaceStanza is : _interfaceStanzas) {

         if (is.get_stanzaStatus() == StanzaStatusType.ACTIVE) {
            is.postProcessStanza();
         }
         addIgnoredStatements(is.get_ignoredStatements());
         _interfaces.addAll(is.get_interfaces());
      }     
      set_alreadyAggregated(false);
      super.postProcessStanza();
   }

	@Override
	public JStanzaType getType() {
		return JStanzaType.INTERFACES;
	}

}