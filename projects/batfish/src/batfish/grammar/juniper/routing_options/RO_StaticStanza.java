package batfish.grammar.juniper.routing_options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import batfish.representation.juniper.StaticOptions;

public class RO_StaticStanza extends ROStanza {
   
   private Map<String, List<StaticOptions>> _staticRoutes;
   private List<StaticOptions> _defaultOptions;
   private String _ribGroup;
   
   private List<RO_STStanza> _rostStanzas;
   
   /* ------------------------------ Constructor ----------------------------*/
   public RO_StaticStanza() {
      _rostStanzas= new ArrayList<RO_STStanza>();      
   }
   
   /* ----------------------------- Other Methods ---------------------------*/
   public void AddROSTStanza (RO_STStanza rost) {
      _rostStanzas.add(rost);
   }

   /* ---------------------------- Getters/Setters --------------------------*/
   public Map<String, List<StaticOptions>> get_staticRoutes () {
      return _staticRoutes;
   }
   
   /* --------------------------- Inherited Methods -------------------------*/  
   @Override
   public void postProcessStanza() {
      super.postProcessStanza();
      
      _staticRoutes = new HashMap<String, List<StaticOptions>> ();
      _defaultOptions = new ArrayList<StaticOptions>();
      
      for (RO_STStanza rost : _rostStanzas) {
         
         rost.postProcessStanza();
         
         switch (rost.getType()) {
         
         case DEFAULTS:
            ROST_DefaultsStanza drost = (ROST_DefaultsStanza) rost;
            _defaultOptions.addAll(drost.get_staticOptions());
            //TODO [Ask Ari]: what to do with these
            break;
            
         case RIB_GROUP:
            ROST_RibGroupStanza rirost = (ROST_RibGroupStanza) rost;
            _ribGroup = rirost.get_groupName();
            //TODO [Ask Ari]: what to do with these
            break;

         case ROUTE:
            ROST_RouteStanza rrost = (ROST_RouteStanza) rost;
            _staticRoutes.put(rrost.get_ip(),rrost.get_staticOptions());
            //TODO [Ask Ari]: what to do with these
            break;
         
         default:
            throw new Error("bad interface stanza type");
         }
         this.addIgnoredStatements(rost.get_ignoredStatements());
      }
   }
      

   @Override
   public ROType getType() {
      return ROType.STATIC;
   }
}
