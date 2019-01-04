package org.batfish.representation.juniper;

import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.Transformation;
import org.junit.Test;

/** Tests for {@link NatRuleSet}. */
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

  @Test
  public void testToTransformation() {
    Prefix prefix1 = Prefix.parse("1.1.1.0/24");
    Prefix prefix2 = Prefix.parse("2.2.2.0/24");

    NatRule natRule1 = new NatRule("natRule1");
    natRule1.getMatches().add(new NatRuleMatchDstAddr(prefix1));
    natRule1.setThen(NatRuleThenOff.INSTANCE);

    NatRule natRule2 = new NatRule("natRule2");
    natRule2.getMatches().add(new NatRuleMatchDstAddr(prefix2));
    natRule2.setThen(new NatRuleThenPool("POOL"));

    NatRuleSet ruleSet = new NatRuleSet("ruleset");
    ruleSet.getRules().add(natRule1);
    ruleSet.getRules().add(natRule2);

    NatPool pool = new NatPool();
    Ip poolStart = Ip.parse("3.0.0.0");
    Ip poolEnd = Ip.parse("4.0.0.0");
    pool.setFromAddress(poolStart);
    pool.setToAddress(poolEnd);

    // the transformation to apply after any NatRule transformation is applied.
    Transformation andThen = when(matchSrcInterface("IFACE")).apply().build();

    assertThat(
        ruleSet.toTransformation(IpField.DESTINATION, ImmutableMap.of("POOL", pool), andThen).get(),
        equalTo(
            // first apply natRule1
            when(matchDst(prefix1))
                .apply()
                .setAndThen(andThen)
                .setOrElse(
                    // only apply natRule2 if natRule1 doesn't match
                    when(matchDst(prefix2))
                        .apply(assignDestinationIp(poolStart, poolEnd))
                        .setAndThen(andThen)
                        .build())
                .build()));
  }
}
