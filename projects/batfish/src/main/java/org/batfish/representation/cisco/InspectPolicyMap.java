package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import org.batfish.datamodel.LineAction;

public class InspectPolicyMap implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private LineAction _classDefaultAction;

  private Map<String, InspectPolicyMapInspectClass> _inspectClasses;

  private final String _name;

  public InspectPolicyMap(String name) {
    _name = name;
    _classDefaultAction = LineAction.REJECT;
    _inspectClasses = new TreeMap<>();
  }

  public LineAction getClassDefaultAction() {
    return _classDefaultAction;
  }

  public Map<String, InspectPolicyMapInspectClass> getInspectClasses() {
    return _inspectClasses;
  }

  public String getName() {
    return _name;
  }

  public void setClassDefaultAction(LineAction classDefaultAction) {
    _classDefaultAction = classDefaultAction;
  }
}
