package org.batfish.question.specifiers;

import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.datamodel.FlowDisposition.DELIVERED_TO_SUBNET;
import static org.batfish.datamodel.FlowDisposition.EXITS_NETWORK;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.batfish.specifier.InferFromLocationIpSpaceSpecifier;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.Location;
import org.batfish.specifier.MockSpecifierContext;
import org.batfish.specifier.NoNodesNodeSpecifier;
import org.batfish.specifier.SpecifierFactories;
import org.batfish.specifier.SpecifierFactories.Version;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test how {@link SpecifiersReachabilityQuestion} constructs different fields of its {@link
 * org.batfish.question.ReachabilityParameters parameters} (defaults, etc).
 */
public class SpecifiersReachabilityQuestionTest {
  @Rule public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testQuestionDefaults() {
    SpecifiersReachabilityQuestion question = new SpecifiersReachabilityQuestion();
    assertThat(
        question.getActions().getDispositions(),
        equalTo(ImmutableSortedSet.of(ACCEPTED, DELIVERED_TO_SUBNET, EXITS_NETWORK)));
    assertThat(
        question.getReachabilityParameters().getDestinationIpSpaceSpecifier(),
        equalTo(new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE)));
    assertThat(
        question.getReachabilityParameters().getSourceIpSpaceSpecifier(),
        equalTo(InferFromLocationIpSpaceSpecifier.INSTANCE));
    assertThat(
        question.getPathConstraints().getTransitLocations(),
        equalTo(NoNodesNodeSpecifier.INSTANCE));
    assertThat(
        question.getPathConstraints().getForbiddenLocations(),
        equalTo(NoNodesNodeSpecifier.INSTANCE));
    assertThat(question.getIgnoreFilters(), equalTo(false));
  }

  @Test
  public void testDestinationIpSpaceSpecification() {
    SpecifiersReachabilityQuestion question =
        SpecifiersReachabilityQuestion.builder()
            .setHeaderConstraints(
                PacketHeaderConstraints.builder().setDstIp("1.2.3.0/24 , 1.2.3.4").build())
            .build();

    Set<Location> locations = ImmutableSet.of(new InterfaceLocation("node", "iface"));

    assertThat(
        question
            .getReachabilityParameters()
            .getDestinationIpSpaceSpecifier()
            .resolve(locations, MockSpecifierContext.builder().build()),
        equalTo(
            IpSpaceAssignment.builder()
                .assign(
                    locations,
                    SpecifierFactories.ACTIVE_VERSION == Version.V1
                        ? IpWildcardSetIpSpace.builder()
                            .including(IpWildcard.parse("1.2.3.0/24"), IpWildcard.parse("1.2.3.4"))
                            .build()
                        : AclIpSpace.union(
                            Prefix.parse("1.2.3.0/24").toIpSpace(),
                            Ip.parse("1.2.3.4").toIpSpace()))
                .build()));
  }

  @Test
  public void testApplicationsSpecification() {
    ImmutableSortedSet<Protocol> applications = ImmutableSortedSet.of(Protocol.DNS);
    SpecifiersReachabilityQuestion question =
        SpecifiersReachabilityQuestion.builder()
            .setHeaderConstraints(
                PacketHeaderConstraints.builder().setApplications(applications).build())
            .build();

    HeaderSpace headerSpace = question.getHeaderSpace();
    assertThat(headerSpace.getDstProtocols(), equalTo(applications));
  }

  @Test
  public void testInvalidApplicationsSpecification() {
    ImmutableSortedSet<Protocol> applications = ImmutableSortedSet.of(Protocol.DNS);
    IntegerSpace dstPorts = IntegerSpace.of(new SubRange(1, 2));

    exception.expect(IllegalArgumentException.class);
    SpecifiersReachabilityQuestion.builder()
        .setHeaderConstraints(
            PacketHeaderConstraints.builder()
                .setDstPorts(dstPorts)
                .setApplications(applications)
                .build())
        .build();
  }

  @Test
  public void testSourceIpWildcardDifferenceAccepted() {
    SpecifiersReachabilityQuestion question =
        SpecifiersReachabilityQuestion.builder()
            .setHeaderConstraints(
                PacketHeaderConstraints.builder().setSrcIp("1.2.3.3 - 1.2.3.4").build())
            .build();

    Set<Location> locations = ImmutableSet.of(new InterfaceLocation("node", "iface"));

    assertThat(
        question
            .getReachabilityParameters()
            .getSourceIpSpaceSpecifier()
            .resolve(locations, MockSpecifierContext.builder().build()),
        equalTo(
            IpSpaceAssignment.builder()
                .assign(
                    locations,
                    SpecifierFactories.ACTIVE_VERSION == Version.V1
                        ? IpWildcardSetIpSpace.builder()
                            .including(IpWildcard.parse("1.2.3.3"))
                            .excluding(IpWildcard.parse("1.2.3.4"))
                            .build()
                        : IpRange.range(Ip.parse("1.2.3.3"), Ip.parse("1.2.3.4")))
                .build()));
  }

  @Test
  public void testTransitAndForbiddenNodesSpecification() {
    SpecifiersReachabilityQuestion question =
        SpecifiersReachabilityQuestion.builder()
            .setPathConstraints(
                PathConstraintsInput.builder()
                    .setTransitLocations("foo")
                    .setForbiddenLocations("bar")
                    .build())
            .build();
    assertThat(
        question.getReachabilityParameters().getRequiredTransitNodesSpecifier(),
        equalTo(
            SpecifierFactories.getNodeSpecifierOrDefault("foo", AllNodesNodeSpecifier.INSTANCE)));
    assertThat(
        question.getReachabilityParameters().getForbiddenTransitNodesSpecifier(),
        equalTo(
            SpecifierFactories.getNodeSpecifierOrDefault("bar", AllNodesNodeSpecifier.INSTANCE)));
  }

  @Test
  public void testIgnoreFilters() {
    SpecifiersReachabilityQuestion q =
        SpecifiersReachabilityQuestion.builder().setIgnoreFilters(true).build();
    assertThat(q.getReachabilityParameters().getIgnoreFilters(), equalTo(true));
  }

  @Test
  public void testInvertSearch() {
    SpecifiersReachabilityQuestion q =
        SpecifiersReachabilityQuestion.builder().setInvertSearch(true).build();
    assertThat(q.getReachabilityParameters().getInvertSearch(), equalTo(true));
  }
}
