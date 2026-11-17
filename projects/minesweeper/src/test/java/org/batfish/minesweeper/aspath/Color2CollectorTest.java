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
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchColor;
import org.batfish.datamodel.routing_policy.expr.MatchColor2;
import org.batfish.datamodel.routing_policy.expr.VarLong;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetColor;
import org.batfish.datamodel.routing_policy.statement.SetColor2;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.minesweeper.utils.Tuple;
import org.junit.Test;

/** Tests for {@link Color2Collector}. */
public class Color2CollectorTest {

  private static Set<Long> collect(Statement... statements) {
    Configuration config =
        new NetworkFactory()
            .configurationBuilder()
            .setHostname("hostname")
            .setConfigurationFormat(ConfigurationFormat.JUNIPER)
            .build();
    return Color2Collector.instance()
        .visitAll(ImmutableList.copyOf(statements), new Tuple<>(new HashSet<>(), config));
  }

  @Test
  public void testCollectsSetLiteral() {
    assertThat(collect(new SetColor2(new LiteralLong(5))), containsInAnyOrder(5L));
  }

  @Test
  public void testCollectsMatch() {
    assertThat(collect(new If(new MatchColor2(7), ImmutableList.of())), containsInAnyOrder(7L));
  }

  @Test
  public void testIgnoresNonLiteral() {
    assertThat(collect(new SetColor2(new VarLong("var"))), empty());
  }

  @Test
  public void testIgnoresColor() {
    assertThat(
        collect(new If(new MatchColor(7), ImmutableList.of()), new SetColor(new LiteralLong(5))),
        empty());
  }
}
