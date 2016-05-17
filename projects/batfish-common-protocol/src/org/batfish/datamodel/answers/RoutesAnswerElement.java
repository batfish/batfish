package org.batfish.datamodel.answers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PrecomputedRoute;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class RoutesAnswerElement implements AnswerElement {

   private Set<PrecomputedRoute> _routes;

   private Map<String, Set<PrecomputedRoute>> _routesByHostname;

   public RoutesAnswerElement(Map<String, Configuration> configurations) {
      _routes = new TreeSet<PrecomputedRoute>();
      _routesByHostname = new TreeMap<String, Set<PrecomputedRoute>>();
      for (Entry<String, Configuration> e : configurations.entrySet()) {
         String hostname = e.getKey();
         Configuration c = e.getValue();
         Set<PrecomputedRoute> routes = c.getRoutes();
         _routes.addAll(routes);
         _routesByHostname.put(hostname, routes);
      }
   }

   public Set<PrecomputedRoute> getRoutes() {
      return _routes;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Map<String, Set<PrecomputedRoute>> getRoutesByHostname() {
      return _routesByHostname;
   }

   public void setRoutes(Set<PrecomputedRoute> routes) {
      _routes = routes;
   }

   public void setRoutesByHostname(
         Map<String, Set<PrecomputedRoute>> routesByHostname) {
      _routesByHostname = routesByHostname;
   }

}
