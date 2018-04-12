package org.batfish.representation.juniper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.batfish.common.util.ReferenceCountedStructure;

public final class FirewallFilter extends ReferenceCountedStructure {

  /** */
  private static final long serialVersionUID = 1L;

  private final int _definitionLine;

  private final Family _family;

  private final Set<String> _fromZones;

  private final String _name;

  private boolean _routingPolicy;

  private final Map<String, FwTerm> _terms;

  public FirewallFilter(String name, Family family, int definitionLine) {
    _definitionLine = definitionLine;
    _family = family;
    _fromZones = new TreeSet<>();
    _name = name;
    _terms = new LinkedHashMap<>();
  }

  public int getDefinitionLine() {
    return _definitionLine;
  }

  public Family getFamily() {
    return _family;
  }

  public Set<String> getFromZones() {
    return _fromZones;
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
}
