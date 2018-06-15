package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;

public final class FirewallFilter implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private final Family _family;

  /*
   * This is important filtering information for security policies (i.e. security policies only
   * apply to specific from-zones)
   */
  private String _fromZone;

  private final String _name;

  private boolean _routingPolicy;

  private final Map<String, FwTerm> _terms;

  public FirewallFilter(String name, Family family) {
    _family = family;
    _fromZone = null;
    _name = name;
    _terms = new LinkedHashMap<>();
  }

  public Family getFamily() {
    return _family;
  }

  public @Nullable String getFromZone() {
    return _fromZone;
  }

  public String getName() {
    return _name;
  }

  public boolean getRoutingPolicy() {
    return _routingPolicy;
  }

  public Map<String, FwTerm> getTerms() {
    return _terms;
  }

  public void setRoutingPolicy(boolean routingPolicy) {
    _routingPolicy = routingPolicy;
  }

  public void setFromZone(String fromZone) {
    _fromZone = fromZone;
  }
}
