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

   protected final RoutingInstance _defaultRoutingInstance;

   protected final Map<String, FirewallFilter> _inetFilters;

   protected final Map<String, PolicyStatement> _policyStatements;

   protected final Map<String, PrefixList> _prefixLists;

   protected final Map<String, RoutingInstance> _routingInstances;

   public JuniperConfiguration() {
      _defaultRoutingInstance = new RoutingInstance(DEFAULT_ROUTING_INSTANCE);
      _inetFilters = new TreeMap<String, FirewallFilter>();
      _prefixLists = new TreeMap<String, PrefixList>();
      _policyStatements = new TreeMap<String, PolicyStatement>();
      _routingInstances = new TreeMap<String, RoutingInstance>();
   }

   public final RoutingInstance getDefaultRoutingInstance() {
      return _defaultRoutingInstance;
   }

   public final Map<String, FirewallFilter> getFirewallFilters() {
      return _inetFilters;
   }

   public final Map<String, PolicyStatement> getPolicyStatements() {
      return _policyStatements;
   }

   public final Map<String, PrefixList> getPrefixLists() {
      return _prefixLists;
   }

   public final Map<String, RoutingInstance> getRoutingInstances() {
      return _routingInstances;
   }

}
