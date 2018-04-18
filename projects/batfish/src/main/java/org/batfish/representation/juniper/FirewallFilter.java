package org.batfish.representation.juniper;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.common.util.ReferenceCountedStructure;

public final class FirewallFilter extends ReferenceCountedStructure {

  /** */
  private static final long serialVersionUID = 1L;

  private final int _definitionLine;

  private final Family _family;

  /*
   * This is important filtering information for security policies (i.e. security policies only
   * apply to specific from-zones)
   */
  private String _fromZone;

  private final String _name;

  private boolean _routingPolicy;

  private final Map<String, FwTerm> _terms;

  public FirewallFilter(String name, Family family, int definitionLine) {
    _definitionLine = definitionLine;
    _family = family;
    _fromZone = null;
    _name = name;
    _terms = new LinkedHashMap<>();
  }

  public int getDefinitionLine() {
    return _definitionLine;
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
