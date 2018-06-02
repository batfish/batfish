package org.batfish.representation.cisco;

import java.util.Map;
import java.util.TreeMap;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.LineAction;

public class InspectPolicyMap extends ComparableStructure<String> {

  /** */
  private static final long serialVersionUID = 1L;

  private LineAction _classDefaultAction;

  private Map<String, InspectPolicyMapInspectClass> _inspectClasses;

  public InspectPolicyMap(String name) {
    super(name);
    _classDefaultAction = LineAction.REJECT;
    _inspectClasses = new TreeMap<>();
  }

  public LineAction getClassDefaultAction() {
    return _classDefaultAction;
  }

  public Map<String, InspectPolicyMapInspectClass> getInspectClasses() {
    return _inspectClasses;
  }

  public void setClassDefaultAction(LineAction classDefaultAction) {
    _classDefaultAction = classDefaultAction;
  }
}
