package org.batfish.minesweeper.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.batfish.minesweeper.bdd.BDDTunnelEncapsulationAttribute.Value;
import org.junit.Test;

public class ModelGenerationTest {
  @Test
  public void testSatAssignmentToTunnelEncapsulationAttribute() {
    TunnelEncapsulationAttribute attr =
        new TunnelEncapsulationAttribute(Ip.FIRST_CLASS_A_PRIVATE_IP);
    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Prefix p10_8 = Prefix.parse("10.0.0.0/8");
    RoutingPolicy policy =
        RoutingPolicy.builder()
            .setName("test")
            .setOwner(c)
            .addStatement(
                new If(
                    new MatchPrefixSet(
                        DestinationNetwork.instance(),
                        new ExplicitPrefixSet(new PrefixSpace(PrefixRange.fromPrefix(p10_8)))),
                    ImmutableList.of(
                        new SetTunnelEncapsulationAttribute(
                            new LiteralTunnelEncapsulationAttribute(attr)))))
            .addStatement(new StaticStatement(Statements.ExitAccept))
            .build();
    ConfigAtomicPredicates configAPs =
        new ConfigAtomicPredicates(
            ImmutableList.of(new SimpleImmutableEntry<>(c, ImmutableList.of(policy))),
            ImmutableSet.of(),
            ImmutableSet.of());

    TransferBDD tbdd = new TransferBDD(configAPs);
    BDDFactory factory = tbdd.getFactory();
    BDDRoute route = new BDDRoute(factory, configAPs);
    List<TransferReturn> paths = tbdd.computePaths(policy, true);
    assertThat(paths, hasSize(2));

    BDD matches10_8 =
        route.getPrefix().toBDD(p10_8).and(route.getPrefixLength().value(p10_8.getPrefixLength()));
    {
      List<TransferReturn> matchingPaths =
          paths.stream()
              .filter(p -> p.getInputConstraints().andSat(matches10_8))
              .collect(Collectors.toList());
      assertThat(matchingPaths, hasSize(1));
      TransferReturn result = matchingPaths.get(0);
      assertThat(
          result
              .getOutputRoute()
              .getTunnelEncapsulationAttribute()
              .satAssignmentToValue(factory.one()),
          equalTo(Value.literal(attr)));
    }

    {
      List<TransferReturn> otherPaths =
          paths.stream()
              .filter(p -> p.getInputConstraints().diffSat(matches10_8))
              .collect(Collectors.toList());
      assertThat(otherPaths, hasSize(1));
      TransferReturn result = otherPaths.get(0);
      assertThat(
          result
              .getOutputRoute()
              .getTunnelEncapsulationAttribute()
              .satAssignmentToValue(factory.one()),
          equalTo(Value.absent()));
    }
  }
}
