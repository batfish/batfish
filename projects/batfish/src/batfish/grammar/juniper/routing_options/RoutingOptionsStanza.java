package batfish.grammar.juniper.routing_options;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import batfish.grammar.juniper.JStanza;
import batfish.grammar.juniper.JStanzaType;
import batfish.grammar.juniper.StanzaStatusType;
import batfish.representation.juniper.StaticOptions;

public class RoutingOptionsStanza extends JStanza {
   
   private List<ROStanza> _roStanzas;
 
   private int _asNum;
   private String _routerId;

   private Map<String, List<StaticOptions>> _staticRoutes;
   private Map<String, List<String>> _ribGroups;
   
   /* ------------------------------ Constructor ----------------------------*/
   public RoutingOptionsStanza() {
      _roStanzas = new ArrayList<ROStanza> ();
      set_postProcessTitle("Routing Options");
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void AddROStanza (ROStanza ro) {
      _roStanzas.add(ro);
   }

   /* ---------------------------- Getters/Setters --------------------------*/
   public Map<String, List<StaticOptions>> get_staticRoutes () {
	   return _staticRoutes;
   }
   public int get_asNum () {
	   return _asNum;
   }
   public String get_routerId () {
	   return _routerId;
   }
   /* --------------------------- Inherited Methods -------------------------*/
   @Override
   public void postProcessStanza() {
      
      _asNum = 0;
      _routerId = "";
            
      for (ROStanza rs : _roStanzas) {
         rs.postProcessStanza();
         
         if (rs.get_stanzaStatus() == StanzaStatusType.ACTIVE) {
         
            switch (rs.getType()) {
            
            case AS:
               RO_AutonomousSystemStanza asros = (RO_AutonomousSystemStanza) rs;
               _asNum = asros.get_asNum();
               break;
               
            case MARTIAN:
               RO_MartiansStanza mros = (RO_MartiansStanza) rs;
               // TODO [Ask Ari]: what to do 
               break;
   
            case RIB:
               RO_RibStanza riros = (RO_RibStanza) rs;
               // TODO [Ask Ari]: what to do 
               break;
               
            case RIB_GROUPS:
               RO_RibGroupsStanza rgros = (RO_RibGroupsStanza) rs;
               _ribGroups = rgros.get_groupsImports();
               // TODO [Ask Ari]: what to do 
               break;
               
            case ROUTER_ID:
               RO_RouterIDStanza rros = (RO_RouterIDStanza) rs;
               _routerId = rros.get_routerID();
               break;
               
            case STATIC:
               RO_StaticStanza sros = (RO_StaticStanza) rs;
               _staticRoutes = sros.get_staticRoutes();
               break;
   
            case NULL:
               break;
   
            default:
                throw new Error ("bad routing-options stanza type");
            }
         }
         this.addIgnoredStatements(rs.get_ignoredStatements());
      }
      set_alreadyAggregated(false);
      super.postProcessStanza();
      
   }
   
	@Override
	public JStanzaType getType() {
		return JStanzaType.ROUTING_OPTIONS;
	}

}
