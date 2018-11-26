package org.batfish.question.specifiers;

import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.datamodel.FlowDisposition.DELIVERED_TO_SUBNET;
import static org.batfish.datamodel.FlowDisposition.EXITS_NETWORK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSortedSet;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.Protocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.batfish.specifier.FlexibleNodeSpecifierFactory;
import org.batfish.specifier.InferFromLocationIpSpaceSpecifier;
import org.batfish.specifier.NoNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifierFactory;
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
                PacketHeaderConstraints.builder().setDstIp("1.2.3.0/24 - 1.2.3.4").build())
            .build();

    assertThat(
        question.getReachabilityParameters().getDestinationIpSpaceSpecifier(),
        equalTo(
            new ConstantIpSpaceSpecifier(
                IpWildcardSetIpSpace.builder()
                    .including(new IpWildcard("1.2.3.0/24"))
                    .excluding(new IpWildcard("1.2.3.4"))
                    .build())));
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
                PacketHeaderConstraints.builder().setSrcIp("1.2.3.0/24 \\ 1.2.3.4").build())
            .build();
    assertThat(
        question.getReachabilityParameters().getSourceIpSpaceSpecifier(),
        equalTo(
            new ConstantIpSpaceSpecifier(
                IpWildcardSetIpSpace.builder()
                    .including(new IpWildcard("1.2.3.0/24"))
                    .excluding(new IpWildcard("1.2.3.4"))
                    .build())));
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
            NodeSpecifierFactory.load(FlexibleNodeSpecifierFactory.NAME)
                .buildNodeSpecifier("foo")));
    assertThat(
        question.getReachabilityParameters().getForbiddenTransitNodesSpecifier(),
        equalTo(
            NodeSpecifierFactory.load(FlexibleNodeSpecifierFactory.NAME)
                .buildNodeSpecifier("bar")));
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
