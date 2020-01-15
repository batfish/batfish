package org.batfish.main;

import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.main.ReachabilityParametersResolver.resolveReachabilityParameters;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.SortedMap;
import java.util.regex.Pattern;
import org.batfish.common.NetworkSnapshot;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpIpSpace;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.questions.InvalidReachabilityParametersException;
import org.batfish.question.ReachabilityParameters;
import org.batfish.question.ReachabilityParameters.Builder;
import org.batfish.question.ResolvedReachabilityParameters;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.batfish.specifier.NodeNameRegexInterfaceLinkLocationSpecifier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests of {@link ReachabilityParametersResolver}. */
public class ReachabilityParametersResolverTest {
  @Rule public TemporaryFolder _tempFolder = new TemporaryFolder();
  @Rule public ExpectedException exception = ExpectedException.none();

  private Batfish _batfish;

  private Configuration _node;
  private NetworkSnapshot _snapshot;

  @Before
  public void setup() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    _node = nf.configurationBuilder().setConfigurationFormat(CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(_node).build();
    nf.interfaceBuilder().setOwner(_node).setVrf(vrf).build();
    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(_node.getHostname(), _node);
    _batfish = BatfishTestUtils.getBatfish(configs, _tempFolder);
    _snapshot = _batfish.getSnapshot();
    _batfish.computeDataPlane(_snapshot);
  }

  @Test
  public void testDestinationIpSpace() throws InvalidReachabilityParametersException {
    Builder reachabilityParametersBuilder =
        ReachabilityParameters.builder()
            .setActions(ImmutableSortedSet.of(ACCEPTED))
            .setSourceIpSpaceSpecifier(new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE))
            .setFinalNodesSpecifier(AllNodesNodeSpecifier.INSTANCE);

    // test default destination IpSpace
    ReachabilityParameters reachabilityParameters = reachabilityParametersBuilder.build();
    ResolvedReachabilityParameters resolvedReachabilityParameters =
        resolveReachabilityParameters(_batfish, reachabilityParameters, _snapshot);
    assertThat(
        resolvedReachabilityParameters.getHeaderSpace(),
        equalTo(
            AclLineMatchExprs.and(
                AclLineMatchExprs.TRUE, AclLineMatchExprs.matchDst(UniverseIpSpace.INSTANCE))));

    // test setting destination IpSpace
    IpIpSpace dstIpSpace = Ip.parse("1.1.1.1").toIpSpace();
    reachabilityParameters =
        reachabilityParametersBuilder
            .setDestinationIpSpaceSpecifier(new ConstantIpSpaceSpecifier(dstIpSpace))
            .build();
    resolvedReachabilityParameters =
        resolveReachabilityParameters(_batfish, reachabilityParameters, _snapshot);
    assertThat(
        resolvedReachabilityParameters.getHeaderSpace(),
        equalTo(
            AclLineMatchExprs.and(AclLineMatchExprs.TRUE, AclLineMatchExprs.matchDst(dstIpSpace))));
  }

  @Test
  public void testResolveDestinationIpSpace_emptyDestIpSpace()
      throws InvalidReachabilityParametersException {
    ReachabilityParameters reachabilityParameters =
        ReachabilityParameters.builder()
            .setActions(ImmutableSortedSet.of(ACCEPTED))
            .setFinalNodesSpecifier(AllNodesNodeSpecifier.INSTANCE)
            .setDestinationIpSpaceSpecifier(new ConstantIpSpaceSpecifier(EmptyIpSpace.INSTANCE))
            .build();

    exception.expect(InvalidReachabilityParametersException.class);
    exception.expectMessage("Empty destination IpSpace");
    new ReachabilityParametersResolver(_batfish, reachabilityParameters, _snapshot)
        .resolveDestinationIpSpace();
  }

  @Test
  public void testResolveSourceIpSpace_emptyLocations()
      throws InvalidReachabilityParametersException {
    ReachabilityParameters reachabilityParameters =
        ReachabilityParameters.builder()
            .setActions(ImmutableSortedSet.of(ACCEPTED))
            .setFinalNodesSpecifier(AllNodesNodeSpecifier.INSTANCE)
            .setDestinationIpSpaceSpecifier(new ConstantIpSpaceSpecifier(EmptyIpSpace.INSTANCE))
            .setSourceLocationSpecifier(
                new NodeNameRegexInterfaceLinkLocationSpecifier(Pattern.compile("")))
            .build();

    ReachabilityParametersResolver resolver =
        new ReachabilityParametersResolver(_batfish, reachabilityParameters, _snapshot);

    exception.expect(InvalidReachabilityParametersException.class);
    exception.expectMessage("No matching source locations");
    resolver.resolveSourceIpSpaceAssignment();
  }

  @Test
  public void testResolveSourceIpSpace_emptyIpSpace()
      throws InvalidReachabilityParametersException {
    ReachabilityParameters reachabilityParameters =
        ReachabilityParameters.builder()
            .setActions(ImmutableSortedSet.of(ACCEPTED))
            .setFinalNodesSpecifier(AllNodesNodeSpecifier.INSTANCE)
            .setDestinationIpSpaceSpecifier(new ConstantIpSpaceSpecifier(EmptyIpSpace.INSTANCE))
            .setSourceLocationSpecifier(
                new NodeNameRegexInterfaceLinkLocationSpecifier(Pattern.compile(".*")))
            .build();

    ReachabilityParametersResolver reachabilityParametersResolver =
        new ReachabilityParametersResolver(_batfish, reachabilityParameters, _snapshot);

    exception.expect(InvalidReachabilityParametersException.class);
    exception.expectMessage("All sources have empty source IpSpaces");
    reachabilityParametersResolver.resolveSourceIpSpaceAssignment();
  }

  @Test
  public void testResolveReachabilityParameters_conflictingTransitNodes()
      throws InvalidReachabilityParametersException {
    // all required transit nodes are also forbidden
    ReachabilityParameters reachabilityParameters =
        ReachabilityParameters.builder()
            .setActions(ImmutableSortedSet.of(ACCEPTED))
            .setFinalNodesSpecifier(AllNodesNodeSpecifier.INSTANCE)
            .setDestinationIpSpaceSpecifier(new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE))
            .setRequiredTransitNodesSpecifier(AllNodesNodeSpecifier.INSTANCE)
            .setForbiddenTransitNodesSpecifier(AllNodesNodeSpecifier.INSTANCE)
            .setSourceLocationSpecifier(
                new NodeNameRegexInterfaceLinkLocationSpecifier(Pattern.compile(".*")))
            .build();

    exception.expect(InvalidReachabilityParametersException.class);
    exception.expectMessage("All required transit nodes are also forbidden");
    resolveReachabilityParameters(_batfish, reachabilityParameters, _snapshot);
  }
}
