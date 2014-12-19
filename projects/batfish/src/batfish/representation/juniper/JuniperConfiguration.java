package batfish.representation.juniper;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class JuniperConfiguration implements Serializable {

   private static final String DEFAULT_ROUTING_INSTANCE = "<default-routing-instance>";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   protected RoutingInstance _defaultRoutingInstance;

   private Map<String, PolicyStatement> _policyStatements;

   private Map<String, PrefixList> _prefixLists;

   protected final Map<String, RoutingInstance> _routingInstances;

   public JuniperConfiguration() {
      _defaultRoutingInstance = new RoutingInstance(DEFAULT_ROUTING_INSTANCE);
      _prefixLists = new TreeMap<String, PrefixList>();
      _policyStatements = new TreeMap<String, PolicyStatement>();
      _routingInstances = new TreeMap<String, RoutingInstance>();
   }

   public RoutingInstance getDefaultRoutingInstance() {
      return _defaultRoutingInstance;
   }

   public Map<String, PolicyStatement> getPolicyStatements() {
      return _policyStatements;
   }

   public Map<String, PrefixList> getPrefixLists() {
      return _prefixLists;
   }

   public Map<String, RoutingInstance> getRoutingInstances() {
      return _routingInstances;
   }

}
