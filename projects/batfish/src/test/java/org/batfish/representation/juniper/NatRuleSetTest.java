package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class NatRuleSetTest {
  private void setLocation(
      NatPacketLocation location, NatPacketLocation.Type type, String direction) {
    switch (type) {
      case InterfaceType:
        location.setInterface(String.format("Interface-%s", direction));
        break;
      case ZoneType:
        location.setZone(String.format("Zone-%s", direction));
        break;
      case RoutingInstanceType:
        location.setRoutingInstance(String.format("RoutingInstance-%s", direction));
        break;
      default:
        return;
    }
  }

  @Test
  public void testComparator() {
    List<NatRuleSet> orderedRuleSetsList = new ArrayList<>();
    List<NatRuleSet> reverseOrderedRuleSetsList = new ArrayList<>();

    int i = 0;
    for (NatPacketLocation.Type toType : NatPacketLocation.Type.values()) {
      for (NatPacketLocation.Type fromType : NatPacketLocation.Type.values()) {
        NatRuleSet ruleSet = new NatRuleSet("ruleSet" + i);
        setLocation(ruleSet.getToLocation(), toType, "to" + i);
        setLocation(ruleSet.getFromLocation(), fromType, "from" + i);
        orderedRuleSetsList.add(ruleSet);
        reverseOrderedRuleSetsList.add(0, ruleSet);
        i++;
      }
    }

    reverseOrderedRuleSetsList.sort(NatRuleSet::compareTo);
    assertThat(orderedRuleSetsList, contains(reverseOrderedRuleSetsList.toArray()));
  }
}
