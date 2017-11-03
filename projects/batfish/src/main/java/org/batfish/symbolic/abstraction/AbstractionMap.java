package org.batfish.symbolic.abstraction;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AbstractionMap {

  private Map<Integer, Set<String>> _canonicalChoiceMap;

  private Map<String, Integer> _groupMap;

  AbstractionMap(Map<Integer, Set<String>> choiceMap, Map<String, Integer> groupMap) {
    this._canonicalChoiceMap = choiceMap;
    this._groupMap = groupMap;
  }

  public Set<String> getAbstractRepresentatives(String router) {
    Integer idx = _groupMap.get(router);
    Set<String> result = _canonicalChoiceMap.get(idx);
    if (result == null) {
      return new HashSet<>();
    }
    return result;
  }
}
