package org.batfish.datamodel.matchers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.EnterInputIfaceStep;
import org.batfish.datamodel.flow.ExitOutputIfaceStep;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.InboundStep;
import org.batfish.datamodel.flow.Step;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

final class HopMatchersImpl {
  private HopMatchersImpl() {}

  static class HasNodeName extends FeatureMatcher<Hop, String> {
    HasNodeName(Matcher<? super String> subMatcher) {
      super(subMatcher, "a Hop with node name:", "node name");
    }

    @Override
    protected String featureValueOf(Hop hop) {
      return hop.getNode().getName();
    }
  }

  static class HasAcceptingInterface extends FeatureMatcher<Hop, NodeInterfacePair> {
    HasAcceptingInterface(Matcher<? super NodeInterfacePair> subMatcher) {
      super(subMatcher, "a Hop with accepting interface:", "accepting interface");
    }

    @Override
    protected NodeInterfacePair featureValueOf(Hop hop) {
      List<Step<?>> steps =
          hop.getSteps().stream()
              .filter(step -> step instanceof InboundStep)
              .collect(ImmutableList.toImmutableList());
      assertThat(steps, hasSize(1));

      InboundStep lastStep = (InboundStep) steps.get(0);
      return NodeInterfacePair.of(hop.getNode().getName(), lastStep.getDetail().getInterface());
    }
  }

  static class HasEnterInputInterface extends FeatureMatcher<Hop, NodeInterfacePair> {
    HasEnterInputInterface(Matcher<? super NodeInterfacePair> subMatcher) {
      super(subMatcher, "a Hop with input interface:", "input interface");
    }

    @Override
    protected NodeInterfacePair featureValueOf(Hop hop) {
      List<Step<?>> steps = hop.getSteps();
      assertThat(steps, not(empty()));
      Step<?> firstStep = steps.get(0);
      assertThat(firstStep, instanceOf(EnterInputIfaceStep.class));
      return ((EnterInputIfaceStep) firstStep).getDetail().getInputInterface();
    }
  }

  static class HasExitOutputInterface extends FeatureMatcher<Hop, NodeInterfacePair> {
    HasExitOutputInterface(Matcher<? super NodeInterfacePair> subMatcher) {
      super(subMatcher, "a Hop with output interface:", "output interface");
    }

    @Override
    protected NodeInterfacePair featureValueOf(Hop hop) {
      List<Step<?>> steps =
          hop.getSteps().stream()
              .filter(step -> step instanceof ExitOutputIfaceStep)
              .collect(ImmutableList.toImmutableList());

      assertThat(steps, hasSize(1));

      ExitOutputIfaceStep lastStep = (ExitOutputIfaceStep) steps.get(0);

      return lastStep.getDetail().getOutputInterface();
    }
  }

  static class HasSteps extends FeatureMatcher<Hop, List<Step<?>>> {
    HasSteps(Matcher<? super List<Step<?>>> subMatcher) {
      super(subMatcher, "a Hop with steps:", "steps");
    }

    @Override
    protected List<Step<?>> featureValueOf(Hop hop) {
      return hop.getSteps();
    }
  }
}
