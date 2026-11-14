package org.batfish.minesweeper.bdd;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.MoreCollectors.onlyElement;
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
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.datamodel.questions.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LiteralTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.expr.MatchColor;
import org.batfish.datamodel.routing_policy.expr.MatchColor2;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchTag2;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetColor;
import org.batfish.datamodel.routing_policy.statement.SetColor2;
import org.batfish.datamodel.routing_policy.statement.SetTag2;
import org.batfish.datamodel.routing_policy.statement.SetTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.statement.Statement;
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

  /** A configuration with a single policy that executes the given statements and accepts. */
  private static RoutingPolicy acceptingPolicy(Statement... statements) {
    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.JUNIPER)
            .build();
    RoutingPolicy.Builder policy = RoutingPolicy.builder().setName("test").setOwner(c);
    for (Statement statement : statements) {
      policy.addStatement(statement);
    }
    return policy.addStatement(new StaticStatement(Statements.ExitAccept)).build();
  }

  private static ConfigAtomicPredicates configAPsFor(RoutingPolicy policy) {
    return new ConfigAtomicPredicates(
        ImmutableList.of(new SimpleImmutableEntry<>(policy.getOwner(), ImmutableList.of(policy))),
        ImmutableSet.of(),
        ImmutableSet.of());
  }

  /** A model of {@code constraints} that is a well-formed BGP route of {@code tbdd}. */
  private static BDD wellFormedModel(BDD constraints, TransferBDD tbdd) {
    BDDRoute route = tbdd.getOriginalRoute();
    return ModelGeneration.constraintsToModel(
        constraints.and(route.wellFormednessConstraints(true)), route);
  }

  @Test
  public void testSatAssignmentToInputRouteLocalAttributes() {
    RoutingPolicy policy =
        acceptingPolicy(
            new If(
                new Conjunction(
                    ImmutableList.of(
                        new MatchTag2(IntComparator.EQ, new LiteralLong(7)),
                        new MatchColor(9),
                        new MatchColor2(11))),
                ImmutableList.of(new StaticStatement(Statements.ExitAccept)),
                ImmutableList.of(new StaticStatement(Statements.ExitReject))));
    ConfigAtomicPredicates configAPs = configAPsFor(policy);
    TransferBDD tbdd = new TransferBDD(configAPs);
    TransferReturn accepted =
        tbdd.computePaths(policy, true).stream()
            .filter(TransferReturn::getAccepted)
            .collect(onlyElement());

    BDD model = wellFormedModel(accepted.getInputConstraints(), tbdd);
    Bgpv4Route input = ModelGeneration.satAssignmentToBgpInputRoute(model, configAPs);

    assertThat(input.getTag2(), equalTo(7L));
    assertThat(input.getColor(), equalTo(9L));
    assertThat(input.getColor2(), equalTo(11L));
  }

  @Test
  public void testSatAssignmentToInputRouteLocalAttributesDefaultToUnset() {
    RoutingPolicy policy = acceptingPolicy(new SetTag2(new LiteralLong(5)));
    ConfigAtomicPredicates configAPs = configAPsFor(policy);
    TransferBDD tbdd = new TransferBDD(configAPs);

    BDD model = wellFormedModel(tbdd.getFactory().one(), tbdd);
    Bgpv4Route input = ModelGeneration.satAssignmentToBgpInputRoute(model, configAPs);

    assertThat(input.getTag2(), equalTo(0L));
    assertThat(input.getColor(), equalTo(0L));
    assertThat(input.getColor2(), equalTo(0L));
  }

  @Test
  public void testSatAssignmentToInputRouteOtherTag2() {
    RoutingPolicy policy = acceptingPolicy(new SetTag2(new LiteralLong(1)));
    ConfigAtomicPredicates configAPs = configAPsFor(policy);
    TransferBDD tbdd = new TransferBDD(configAPs);
    BDDRoute route = tbdd.getOriginalRoute();
    BDD otherTag2 =
        route
            .getTag2()
            .getIsValidConstraint()
            .diff(route.getTag2().value(0))
            .diff(route.getTag2().value(1));

    BDD model = wellFormedModel(otherTag2, tbdd);
    Bgpv4Route input = ModelGeneration.satAssignmentToBgpInputRoute(model, route, configAPs);

    // neither unset (0) nor the known literal 1
    assertThat(input.getTag2(), equalTo(2L));
  }

  @Test
  public void testSatAssignmentToOutputRouteLocalAttributes() {
    RoutingPolicy policy =
        acceptingPolicy(
            new SetTag2(new LiteralLong(5)),
            new SetColor(new LiteralLong(3)),
            new SetColor2(new LiteralLong(13)));
    ConfigAtomicPredicates configAPs = configAPsFor(policy);
    TransferBDD tbdd = new TransferBDD(configAPs);
    TransferReturn path = getOnlyElement(tbdd.computePaths(policy, true));

    BDD model = wellFormedModel(path.getInputConstraints(), tbdd);
    BgpRoute output =
        ModelGeneration.satAssignmentToOutputRoute(
            model, path.getOutputRoute(), configAPs, Environment.Direction.IN);

    assertThat(output.getTag2(), equalTo(5L));
    assertThat(output.getColor(), equalTo(3L));
    assertThat(output.getColor2(), equalTo(13L));
  }
}
