package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.SortedMap;
import java.util.TreeSet;

public class NeighborsDiff extends ConfigDiffElement {

  @JsonCreator
  private NeighborsDiff() {}

  public NeighborsDiff(
      SortedMap<Prefix, ? extends BgpPeerConfig> before,
      SortedMap<Prefix, ? extends BgpPeerConfig> after) {
    super(new TreeSet<>(), new TreeSet<>());

    for (Prefix beforePrefix : before.keySet()) {
      BgpPeerConfig beforeNeighbor = before.get(beforePrefix);
      String beforeDescription = beforeNeighbor.getDescription();
      if (beforeDescription == null) {
        beforeDescription = beforePrefix.toString();
      }
      if (after.containsKey(beforePrefix)) {
        BgpPeerConfig bNeighbor = after.get(beforePrefix);
        if (beforeNeighbor.equals(bNeighbor)) {
          super._identical.add(beforeDescription);
        } else {
          _diff.add(beforeDescription);
        }
      } else {
        super._inBeforeOnly.add(beforeDescription);
      }
    }

    for (Prefix afterPrefix : after.keySet()) {
      BgpPeerConfig afterNeighbor = after.get(afterPrefix);
      if (!before.containsKey(afterPrefix)) {
        String afterDescription = afterNeighbor.getDescription();
        if (afterDescription == null) {
          afterDescription = afterPrefix.toString();
        }
        super._inAfterOnly.add(afterDescription);
      }
    }

    super.summarizeIdentical();
  }
}
