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

   protected final Map<String, RoutingInstance> _routingInstances;

   public JuniperConfiguration() {
      _routingInstances = new TreeMap<String, RoutingInstance>();
      _defaultRoutingInstance = new RoutingInstance(DEFAULT_ROUTING_INSTANCE);
   }

   public RoutingInstance getDefaultRoutingInstance() {
      return _defaultRoutingInstance;
   }

   public Map<String, RoutingInstance> getRoutingInstances() {
      return _routingInstances;
   }

}
