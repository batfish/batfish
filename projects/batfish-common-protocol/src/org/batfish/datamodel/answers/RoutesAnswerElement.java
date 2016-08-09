package org.batfish.datamodel.answers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Route;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RoutesAnswerElement implements AnswerElement {

   private SortedSet<Route> _routes;

   private SortedMap<String, SortedSet<Route>> _routesByHostname;

   @JsonCreator
   public RoutesAnswerElement() {
   }

   public RoutesAnswerElement(Map<String, Configuration> configurations,
         Pattern nodeRegex) {
      _routes = new TreeSet<Route>();
      _routesByHostname = new TreeMap<String, SortedSet<Route>>();
      for (Entry<String, Configuration> e : configurations.entrySet()) {
         String hostname = e.getKey();
         if (!nodeRegex.matcher(hostname).matches()) {
            continue;
         }
         Configuration c = e.getValue();
         SortedSet<Route> routes = c.getRoutes();
         _routes.addAll(routes);
         _routesByHostname.put(hostname, routes);
      }
   }

   public SortedSet<Route> getRoutes() {
      return _routes;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public SortedMap<String, SortedSet<Route>> getRoutesByHostname() {
      return _routesByHostname;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      // TODO: change this function to pretty print the answer
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }

   public void setRoutes(SortedSet<Route> routes) {
      _routes = routes;
   }

   public void setRoutesByHostname(
         SortedMap<String, SortedSet<Route>> routesByHostname) {
      _routesByHostname = routesByHostname;
   }

}
