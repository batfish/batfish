package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.batfish.common.BatfishException;

/** Types of state for a network flow (as recognized by firewalls, for example). */
public enum FlowState {
  ESTABLISHED(1),
  INVALID(3),
  NEW(0),
  RELATED(2);

  private static final Map<Integer, FlowState> _map = buildNumberMap();

  private static final Map<String, FlowState> _nameMap = buildNameMap();

  private static Map<String, FlowState> buildNameMap() {
    ImmutableMap.Builder<String, FlowState> map = ImmutableMap.builder();
    for (FlowState value : FlowState.values()) {
      String name = value.name().toLowerCase();
      map.put(name, value);
    }
    return map.build();
  }

  private static Map<Integer, FlowState> buildNumberMap() {
    ImmutableMap.Builder<Integer, FlowState> map = ImmutableMap.builder();
    for (FlowState value : FlowState.values()) {
      map.put(value._num, value);
    }
    return map.build();
  }

  public static FlowState fromNum(int num) {
    FlowState instance = _map.get(num);
    if (instance == null) {
      throw new BatfishException("Not a valid flow state number: '" + num + "'");
    }
    return instance;
  }

  @JsonCreator
  public static FlowState fromString(String name) {
    FlowState state = _nameMap.get(name.toLowerCase());
    if (state == null) {
      throw new BatfishException("Not a valid flow state name: '" + name + "'");
    }
    return state;
  }

  private final int _num;

  FlowState(int num) {
    _num = num;
  }

  public int number() {
    return _num;
  }
}
