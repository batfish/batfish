package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.List;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Step;
import org.batfish.datamodel.matchers.HopMatchersImpl.HasAcceptingInterface;
import org.batfish.datamodel.matchers.HopMatchersImpl.HasEnterInputInterface;
import org.batfish.datamodel.matchers.HopMatchersImpl.HasExitOutputInterface;
import org.batfish.datamodel.matchers.HopMatchersImpl.HasNodeName;
import org.batfish.datamodel.matchers.HopMatchersImpl.HasSteps;
import org.hamcrest.Matcher;

/** {@link Matcher Matchers} for {@link Hop}. */
public final class HopMatchers {
  public static Matcher<Hop> hasNodeName(String nodeName) {
    return new HasNodeName(equalTo(nodeName));
  }

  public static Matcher<Hop> hasAcceptingInterface(NodeInterfacePair iface) {
    return new HasAcceptingInterface(is(iface));
  }

  public static HasEnterInputInterface hasInputInterface(NodeInterfacePair iface) {
    return new HasEnterInputInterface(is(iface));
  }

  public static HasEnterInputInterface hasInputInterface(
      Matcher<? super NodeInterfacePair> subMatcher) {
    return new HasEnterInputInterface(subMatcher);
  }

  public static HasExitOutputInterface hasOutputInterface(NodeInterfacePair iface) {
    return new HasExitOutputInterface(is(iface));
  }

  public static HasExitOutputInterface hasOutputInterface(
      Matcher<? super NodeInterfacePair> subMatcher) {
    return new HasExitOutputInterface(subMatcher);
  }

  public static HasSteps hasSteps(Matcher<? super List<? extends Step<?>>> subMatcher) {
    return new HasSteps(subMatcher);
  }
}
