package org.batfish.main;

import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.main.ReachabilityParametersResolver.resolveReachabilityParameters;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.regex.Pattern;
import org.batfish.common.NetworkSnapshot;
import org.batfish.datamodel.ConcreteInterfaceAddress;
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
import org.batfish.specifier.ConstantIpSpaceAssignmentSpecifier;
import org.batfish.specifier.NodeNameRegexInterfaceLinkLocationSpecifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests of {@link ReachabilityParametersResolver}. */
class ReachabilityParametersResolverTest {
  @TempDir public File _tempFolder;

  private Batfish _batfish;

  private Configuration _node;
  private NetworkSnapshot _snapshot;

  @BeforeEach
  void setup() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    _node = nf.configurationBuilder().setConfigurationFormat(CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(_node).build();
    nf.interfaceBuilder()
        .setOwner(_node)
        .setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/24"))
        .setVrf(vrf)
        .build();
    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(_node.getHostname(), _node);
    _batfish = BatfishTestUtils.getBatfish(configs, _tempFolder);
    _snapshot = _batfish.getSnapshot();
    _batfish.computeDataPlane(_snapshot);
  }

  @Test
  void testDestinationIpSpace() throws InvalidReachabilityParametersException {
    Builder reachabilityParametersBuilder =
        ReachabilityParameters.builder()
            .setActions(ImmutableSortedSet.of(ACCEPTED))
            .setSourceIpSpaceSpecifier(
                new ConstantIpSpaceAssignmentSpecifier(UniverseIpSpace.INSTANCE))
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
            .setDestinationIpSpaceSpecifier(new ConstantIpSpaceAssignmentSpecifier(dstIpSpace))
            .build();
    resolvedReachabilityParameters =
        resolveReachabilityParameters(_batfish, reachabilityParameters, _snapshot);
    assertThat(
        resolvedReachabilityParameters.getHeaderSpace(),
        equalTo(
            AclLineMatchExprs.and(AclLineMatchExprs.TRUE, AclLineMatchExprs.matchDst(dstIpSpace))));
  }

  @Test
  void testResolveDestinationIpSpace_emptyDestIpSpace() {
    ReachabilityParameters reachabilityParameters =
        ReachabilityParameters.builder()
            .setActions(ImmutableSortedSet.of(ACCEPTED))
            .setFinalNodesSpecifier(AllNodesNodeSpecifier.INSTANCE)
            .setDestinationIpSpaceSpecifier(
                new ConstantIpSpaceAssignmentSpecifier(EmptyIpSpace.INSTANCE))
            .build();

    Throwable exception =
        assertThrows(
            InvalidReachabilityParametersException.class,
            () ->
                new ReachabilityParametersResolver(_batfish, reachabilityParameters, _snapshot)
                    .resolveDestinationIpSpace());
    assertThat(exception.getMessage(), containsString("Empty destination IpSpace"));
  }

  @Test
  void testResolveSourceIpSpace_emptyLocations() {
    ReachabilityParameters reachabilityParameters =
        ReachabilityParameters.builder()
            .setActions(ImmutableSortedSet.of(ACCEPTED))
            .setFinalNodesSpecifier(AllNodesNodeSpecifier.INSTANCE)
            .setDestinationIpSpaceSpecifier(
                new ConstantIpSpaceAssignmentSpecifier(EmptyIpSpace.INSTANCE))
            .setSourceLocationSpecifier(
                new NodeNameRegexInterfaceLinkLocationSpecifier(Pattern.compile("")))
            .build();

    ReachabilityParametersResolver resolver =
        new ReachabilityParametersResolver(_batfish, reachabilityParameters, _snapshot);

    Throwable exception =
        assertThrows(
            InvalidReachabilityParametersException.class,
            () -> resolver.resolveSourceIpSpaceAssignment());
    assertThat(exception.getMessage(), containsString("No matching source locations"));
  }

  @Test
  void testResolveSourceIpSpace_emptyIpSpace() {
    ReachabilityParameters reachabilityParameters =
        ReachabilityParameters.builder()
            .setActions(ImmutableSortedSet.of(ACCEPTED))
            .setFinalNodesSpecifier(AllNodesNodeSpecifier.INSTANCE)
            .setSourceIpSpaceSpecifier(
                new ConstantIpSpaceAssignmentSpecifier(EmptyIpSpace.INSTANCE))
            .setSourceLocationSpecifier(
                new NodeNameRegexInterfaceLinkLocationSpecifier(Pattern.compile(".*")))
            .build();

    ReachabilityParametersResolver reachabilityParametersResolver =
        new ReachabilityParametersResolver(_batfish, reachabilityParameters, _snapshot);

    Throwable exception =
        assertThrows(
            InvalidReachabilityParametersException.class,
            () -> reachabilityParametersResolver.resolveSourceIpSpaceAssignment());
    assertThat(exception.getMessage(), containsString("All sources have empty source IpSpaces"));
  }

  @Test
  void testResolveReachabilityParameters_conflictingTransitNodes() {
    // all required transit nodes are also forbidden
    ReachabilityParameters reachabilityParameters =
        ReachabilityParameters.builder()
            .setActions(ImmutableSortedSet.of(ACCEPTED))
            .setFinalNodesSpecifier(AllNodesNodeSpecifier.INSTANCE)
            .setDestinationIpSpaceSpecifier(
                new ConstantIpSpaceAssignmentSpecifier(UniverseIpSpace.INSTANCE))
            .setRequiredTransitNodesSpecifier(AllNodesNodeSpecifier.INSTANCE)
            .setForbiddenTransitNodesSpecifier(AllNodesNodeSpecifier.INSTANCE)
            .setSourceLocationSpecifier(
                new NodeNameRegexInterfaceLinkLocationSpecifier(Pattern.compile(".*")))
            .build();

    Throwable exception =
        assertThrows(
            InvalidReachabilityParametersException.class,
            () -> resolveReachabilityParameters(_batfish, reachabilityParameters, _snapshot));
    assertThat(
        exception.getMessage(), containsString("All required transit nodes are also forbidden"));
  }
}
