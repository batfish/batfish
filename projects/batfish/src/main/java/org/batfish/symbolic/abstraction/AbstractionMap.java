package org.batfish.symbolic.abstraction;

import java.util.Map;
import java.util.Set;

public class AbstractionMap {

  private Map<Integer, Set<String>> _canonicalChoiceMap;

  private Map<String, Integer> _groupMap;

  public AbstractionMap(Map<Integer, Set<String>> choiceMap, Map<String, Integer> groupMap) {
    this._canonicalChoiceMap = choiceMap;
    this._groupMap = groupMap;
  }

  public Set<String> getRepresentatives(String router) {
    Integer idx = _groupMap.get(router);
    return _canonicalChoiceMap.get(idx);
  }
}
