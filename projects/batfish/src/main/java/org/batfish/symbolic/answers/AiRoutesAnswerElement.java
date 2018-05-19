package org.batfish.symbolic.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.symbolic.ainterpreter.RibEntry;

public class AiRoutesAnswerElement extends AnswerElement {

  private static final String PROP_ROUTES = "routes";

  private static final String PROP_ROUTES_BY_HOSTNAME = "routesByHostname";

  private SortedSet<RibEntry> _routes;

  private SortedMap<String, SortedSet<RibEntry>> _routesByHostname;

  @JsonProperty(PROP_ROUTES)
  public SortedSet<RibEntry> getRoutes() {
    return _routes;
  }

  @JsonProperty(PROP_ROUTES)
  public void setRoutes(SortedSet<RibEntry> x) {
    this._routes = x;
  }

  @JsonProperty(PROP_ROUTES_BY_HOSTNAME)
  public SortedMap<String, SortedSet<RibEntry>> getRoutesByHostname() {
    return _routesByHostname;
  }

  @JsonProperty(PROP_ROUTES_BY_HOSTNAME)
  public void setRoutesByHostname(SortedMap<String, SortedSet<RibEntry>> x) {
    this._routesByHostname = x;
  }

  @Override
  public String prettyPrint() {
    StringBuilder sb = new StringBuilder();
    for (Entry<String, SortedSet<RibEntry>> e : _routesByHostname.entrySet()) {
      String router = e.getKey();
      SortedSet<RibEntry> entries = e.getValue();
      sb.append("Router ").append(router).append("\n");
      for (RibEntry entry : entries) {
        sb.append("  ").append(entry).append("\n");
      }
    }
    return sb.toString();
  }
}
