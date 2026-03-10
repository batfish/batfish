package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.communities.HasSize;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.representation.juniper.PsFromCommunityCount.Mode;
import org.junit.Test;

/** Tests of {@link PsFromCommunityCount}. */
public class PsFromCommunityCountTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new PsFromCommunityCount(5, Mode.EXACT), new PsFromCommunityCount(5, Mode.EXACT))
        .addEqualityGroup(new PsFromCommunityCount(4, Mode.EXACT))
        .addEqualityGroup(new PsFromCommunityCount(4, Mode.ORHIGHER))
        .testEquals();
  }

  @Test
  public void testConversion() { // Set up
    JuniperConfiguration jc = new JuniperConfiguration();
    Configuration c =
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.JUNIPER)
            .setHostname("c")
            .build();
    Warnings w = new Warnings();

    BooleanExpr exact = new PsFromCommunityCount(5, Mode.EXACT).toBooleanExpr(jc, c, w);
    assertThat(
        exact,
        equalTo(
            new MatchCommunities(
                InputCommunities.instance(),
                new HasSize(new IntComparison(IntComparator.EQ, new LiteralInt(5))))));
  }
}
