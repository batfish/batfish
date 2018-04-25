package org.batfish.representation.cisco;

import java.util.Map;
import java.util.TreeMap;
import org.batfish.common.util.DefinedStructure;

public class InspectPolicyMap extends DefinedStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private Map<String, InspectPolicyMapInspectClass> _inspectClasses;

  public InspectPolicyMap(String name, int definitionLine) {
    super(name, definitionLine);
    _inspectClasses = new TreeMap<>();
  }

  public Map<String, InspectPolicyMapInspectClass> getInspectClasses() {
    return _inspectClasses;
  }
}
