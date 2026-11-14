package org.batfish.minesweeper.aspath;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchTag2;
import org.batfish.datamodel.routing_policy.expr.VarLong;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetTag2;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.minesweeper.utils.Tuple;
import org.junit.Test;

/** Tests for {@link Tag2Collector}. */
public class Tag2CollectorTest {

  private static Set<Long> collect(Statement... statements) {
    Configuration config =
        new NetworkFactory()
            .configurationBuilder()
            .setHostname("hostname")
            .setConfigurationFormat(ConfigurationFormat.JUNIPER)
            .build();
    return Tag2Collector.instance()
        .visitAll(ImmutableList.copyOf(statements), new Tuple<>(new HashSet<>(), config));
  }

  private static Statement ifMatch(MatchTag2 match) {
    return new If(match, ImmutableList.of());
  }

  @Test
  public void testCollectsSetLiteral() {
    assertThat(collect(new SetTag2(new LiteralLong(5))), containsInAnyOrder(5L));
  }

  @Test
  public void testCollectsEqualityMatchLiteral() {
    assertThat(
        collect(ifMatch(new MatchTag2(IntComparator.EQ, new LiteralLong(7)))),
        containsInAnyOrder(7L));
  }

  @Test
  public void testCollectsFromMatchesAndSets() {
    assertThat(
        collect(
            ifMatch(new MatchTag2(IntComparator.EQ, new LiteralLong(7))),
            new SetTag2(new LiteralLong(5))),
        containsInAnyOrder(5L, 7L));
  }

  @Test
  public void testIgnoresNonEqualityMatch() {
    assertThat(collect(ifMatch(new MatchTag2(IntComparator.GE, new LiteralLong(7)))), empty());
  }

  @Test
  public void testIgnoresNonLiteral() {
    assertThat(
        collect(
            ifMatch(new MatchTag2(IntComparator.EQ, new VarLong("var"))),
            new SetTag2(new VarLong("var"))),
        empty());
  }
}
