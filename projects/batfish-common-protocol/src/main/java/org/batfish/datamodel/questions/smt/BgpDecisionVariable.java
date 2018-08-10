package org.batfish.datamodel.questions.smt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.common.BatfishException;

public enum BgpDecisionVariable {
  LOCALPREF("localpref"),
  PATHLEN("pathlen"),
  MED("med"),
  IGPCOST("igpcost"),
  EBGP_PREF_IBGP("ebgp_pref_ibgp");

  private static final Map<String, BgpDecisionVariable> _map = buildMap();

  private static Map<String, BgpDecisionVariable> buildMap() {
    ImmutableMap.Builder<String, BgpDecisionVariable> map = ImmutableMap.builder();
    for (BgpDecisionVariable value : BgpDecisionVariable.values()) {
      String name = value._name.toLowerCase();
      map.put(name, value);
    }
    return map.build();
  }

  @JsonCreator
  public static BgpDecisionVariable fromName(String name) {
    BgpDecisionVariable instance = _map.get(name.toLowerCase());
    if (instance == null) {
      throw new BatfishException(
          "No " + BgpDecisionVariable.class.getSimpleName() + " with name: \"" + name + "\"");
    }
    return instance;
  }

  private final String _name;

  BgpDecisionVariable(String name) {
    _name = name;
  }

  @JsonValue
  public String BgpDecisionVariableName() {
    return _name;
  }
}
