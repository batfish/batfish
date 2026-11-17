package org.batfish.datamodel.flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.EnterInputIfaceStep.EnterInputIfaceStepDetail;
import org.batfish.datamodel.flow.ExitOutputIfaceStep.ExitOutputIfaceStepDetail;
import org.batfish.datamodel.flow.InboundStep.InboundStepDetail;
import org.batfish.datamodel.pojo.Node;
import org.junit.jupiter.api.Test;

/** Tests for {@link Trace}. */
class TraceTest {

  private Hop _emptyHop = new Hop(new Node("Empty"), ImmutableList.of());
  private Hop _hopWithOnlyEnter =
      new Hop(
          new Node("Enter"),
          ImmutableList.of(
              EnterInputIfaceStep.builder()
                  .setAction(StepAction.RECEIVED)
                  .setDetail(
                      EnterInputIfaceStepDetail.builder()
                          .setInputInterface(NodeInterfacePair.of("Enter", "in"))
                          .build())
                  .build()));
  private Hop _hopWithOnlyExit =
      new Hop(
          new Node("Exit"),
          ImmutableList.of(
              ExitOutputIfaceStep.builder()
                  .setAction(StepAction.RECEIVED)
                  .setDetail(
                      ExitOutputIfaceStepDetail.builder()
                          .setOutputInterface(NodeInterfacePair.of("Exit", "out"))
                          .build())
                  .build()));
  private Hop _acceptedHop =
      new Hop(
          new Node("Enter"),
          ImmutableList.of(
              EnterInputIfaceStep.builder()
                  .setAction(StepAction.RECEIVED)
                  .setDetail(
                      EnterInputIfaceStepDetail.builder()
                          .setInputInterface(NodeInterfacePair.of("Enter", "in"))
                          .build())
                  .build(),
              InboundStep.builder().setDetail(new InboundStepDetail("Loopback0")).build()));

  @Test
  void validateHopRejectsEmptyHops() {
    Throwable exception =
        assertThrows(Exception.class, () -> Trace.validateHops(ImmutableList.of(_emptyHop)));
    assertThat(exception.getMessage(), containsString("Invalid hop with no steps:"));
  }

  @Test
  void validateHopBeginsWithEnterInputInterface() {
    Throwable exception =
        assertThrows(
            Exception.class,
            () -> Trace.validateHops(ImmutableList.of(_hopWithOnlyExit, _hopWithOnlyExit)));
    assertThat(
        exception.getMessage(),
        containsString("Hop 2/2 of trace does not begin with an EnterInputIfaceStep:"));
  }

  @Test
  void validateHopEndsWithExitOutputInterface() {
    Throwable exception =
        assertThrows(
            Exception.class,
            () -> Trace.validateHops(ImmutableList.of(_hopWithOnlyEnter, _hopWithOnlyEnter)));
    assertThat(
        exception.getMessage(),
        containsString("Hop 1/2 of trace does not end with an ExitOutputIfaceStep:"));
  }

  @Test
  void validateValidHops() {
    Trace.validateHops(ImmutableList.of());
    Trace.validateHops(ImmutableList.of(_hopWithOnlyEnter));
    Trace.validateHops(ImmutableList.of(_hopWithOnlyExit));
    Trace.validateHops(ImmutableList.of(_hopWithOnlyExit, _hopWithOnlyEnter));
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(5L)
        .addEqualityGroup(new Trace(FlowDisposition.ACCEPTED, ImmutableList.of(_acceptedHop)))
        .addEqualityGroup(
            new Trace(FlowDisposition.ACCEPTED, ImmutableList.of(_hopWithOnlyExit, _acceptedHop)))
        .addEqualityGroup(
            new Trace(FlowDisposition.DELIVERED_TO_SUBNET, ImmutableList.of(_hopWithOnlyExit)))
        .addEqualityGroup(
            new Trace(FlowDisposition.EXITS_NETWORK, ImmutableList.of(_hopWithOnlyExit)))
        .testEquals();
  }
}
