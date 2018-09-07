package org.batfish.datamodel.questions.smt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.batfish.common.BatfishException;

/**
 * Represents the criteria that BGP uses to rank paths. It's used to allow to select which criteria
 * are to be used and in what order (diverging from the standard BGP path selection algorithm).
 */
public enum BgpDecisionVariable {
  LOCALPREF("localpref"),
  PATHLEN("pathlen"),
  MED("med"),
  IGPCOST("igpcost"),
  EBGP_PREF_IBGP("ebgp_pref_ibgp");

  private final String _name;

  BgpDecisionVariable(String name) {
    _name = name;
  }

  @JsonCreator
  public static BgpDecisionVariable fromName(String name) throws BatfishException {
    try {
      return BgpDecisionVariable.valueOf(name.toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new BatfishException(
          "No " + BgpDecisionVariable.class.getSimpleName() + " with name: \"" + name + "\"");
    }
  }

  @JsonValue
  public String bgpDecisionVariableName() {
    return _name;
  }
}
