package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Map;

public class VrfsDiff extends ConfigDiffElement {

  @JsonCreator
  public VrfsDiff() {}

  public VrfsDiff(Map<String, Vrf> before, Map<String, Vrf> after) {
    super(before.keySet(), after.keySet());
    for (String name : super.common()) {
      Vrf beforeVrf = before.get(name);
      Vrf afterVrf = after.get(name);
      VrfDiff vrfDiff = new VrfDiff(beforeVrf, afterVrf);
      if (!vrfDiff.isEmpty()) {
        _diff.add(name);
        _diffInfo.put(name, vrfDiff);
      } else {
        _identical.add(name);
      }
    }
  }
}
