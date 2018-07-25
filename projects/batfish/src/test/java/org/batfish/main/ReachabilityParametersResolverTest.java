package org.batfish.main;

import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.batfish.datamodel.ForwardingAction.ACCEPT;
import static org.batfish.main.ReachabilityParametersResolver.resolveReachabilityParameters;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableSet;
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
import org.batfish.datamodel.questions.InvalidReachabilityParametersException;
import org.batfish.question.ReachabilityParameters;
import org.batfish.question.ReachabilityParameters.Builder;
import org.batfish.question.ResolvedReachabilityParameters;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.batfish.specifier.NoNodesNodeSpecifier;
import org.batfish.specifier.NodeNameRegexInterfaceLinkLocationSpecifier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class ReachabilityParametersResolverTest {
  @Rule public TemporaryFolder _tempFolder = new TemporaryFolder();
  @Rule public ExpectedException exception = ExpectedException.none();

  private Batfish _batfish;

  private NetworkSnapshot _snapshot;

  @Before
  public void setup() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration config = nf.configurationBuilder().setConfigurationFormat(CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    nf.interfaceBuilder().setOwner(config).setVrf(vrf).build();
    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(config.getName(), config);
    _batfish = BatfishTestUtils.getBatfish(configs, _tempFolder);
    _batfish.computeDataPlane(false);
    _snapshot = _batfish.getNetworkSnapshot();
  }

  @Test
  public void testDestinationIpSpace() throws InvalidReachabilityParametersException {
    Builder reachabilityParametersBuilder =
        ReachabilityParameters.builder()
            .setActions(ImmutableSortedSet.of(ACCEPT))
            .setSourceIpSpaceSpecifier(new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE))
            .setFinalNodesSpecifier(AllNodesNodeSpecifier.INSTANCE);

    // test default destination IpSpace
    ReachabilityParameters reachabilityParameters = reachabilityParametersBuilder.build();
    ResolvedReachabilityParameters resolvedReachabilityParameters =
        resolveReachabilityParameters(_batfish, reachabilityParameters, _snapshot);
    assertThat(resolvedReachabilityParameters.getHeaderSpace().getNotDstIps(), nullValue());
    assertThat(
        resolvedReachabilityParameters.getHeaderSpace().getDstIps(),
        equalTo(UniverseIpSpace.INSTANCE));

    // test setting destination IpSpace
    IpIpSpace dstIpSpace = new Ip("1.1.1.1").toIpSpace();
    reachabilityParameters =
        reachabilityParametersBuilder
            .setDestinationIpSpaceSpecifier(new ConstantIpSpaceSpecifier(dstIpSpace))
            .build();
    resolvedReachabilityParameters =
        resolveReachabilityParameters(_batfish, reachabilityParameters, _snapshot);
    assertThat(resolvedReachabilityParameters.getHeaderSpace().getNotDstIps(), nullValue());
    assertThat(resolvedReachabilityParameters.getHeaderSpace().getDstIps(), equalTo(dstIpSpace));
  }

  @Test
  public void testResolveDestinationIpSpace_emptyDestIpSpace()
      throws InvalidReachabilityParametersException {
    ReachabilityParameters reachabilityParameters =
        ReachabilityParameters.builder()
            .setActions(ImmutableSortedSet.of(ACCEPT))
            .setFinalNodesSpecifier(AllNodesNodeSpecifier.INSTANCE)
            .setDestinationIpSpaceSpecifier(new ConstantIpSpaceSpecifier(EmptyIpSpace.INSTANCE))
            .build();

    exception.expect(InvalidReachabilityParametersException.class);
    exception.expectMessage("Empty destination IpSpace");
    new ReachabilityParametersResolver(_batfish, reachabilityParameters, _snapshot)
        .resolveDestinationIpSpace();
  }

  @Test
  public void testResolveNodes_null() throws InvalidReachabilityParametersException {
    ReachabilityParameters reachabilityParameters =
        ReachabilityParameters.builder()
            .setActions(ImmutableSortedSet.of(ACCEPT))
            .setFinalNodesSpecifier(null)
            .setDestinationIpSpaceSpecifier(new ConstantIpSpaceSpecifier(EmptyIpSpace.INSTANCE))
            .setSourceLocationSpecifier(
                new NodeNameRegexInterfaceLinkLocationSpecifier(Pattern.compile("")))
            .build();
    ReachabilityParametersResolver resolver =
        new ReachabilityParametersResolver(_batfish, reachabilityParameters, _snapshot);
    assertThat(
        resolver.resolveNodes("foo", reachabilityParameters.getFinalNodesSpecifier()),
        equalTo(ImmutableSet.of()));
  }

  @Test
  public void testResolveNodes_noMatch() throws InvalidReachabilityParametersException {
    ReachabilityParameters reachabilityParameters =
        ReachabilityParameters.builder()
            .setActions(ImmutableSortedSet.of(ACCEPT))
            .setFinalNodesSpecifier(NoNodesNodeSpecifier.INSTANCE)
            .setDestinationIpSpaceSpecifier(new ConstantIpSpaceSpecifier(EmptyIpSpace.INSTANCE))
            .setSourceLocationSpecifier(
                new NodeNameRegexInterfaceLinkLocationSpecifier(Pattern.compile("")))
            .build();
    ReachabilityParametersResolver resolver =
        new ReachabilityParametersResolver(_batfish, reachabilityParameters, _snapshot);
    exception.expect(InvalidReachabilityParametersException.class);
    exception.expectMessage("No nodes match foo specifier");
    resolver.resolveNodes("foo", reachabilityParameters.getFinalNodesSpecifier());
  }

  @Test
  public void testResolveSourceIpSpace_emptyLocations()
      throws InvalidReachabilityParametersException {
    ReachabilityParameters reachabilityParameters =
        ReachabilityParameters.builder()
            .setActions(ImmutableSortedSet.of(ACCEPT))
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
            .setActions(ImmutableSortedSet.of(ACCEPT))
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
            .setActions(ImmutableSortedSet.of(ACCEPT))
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
