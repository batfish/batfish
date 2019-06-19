package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.FlowDiff;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.matchers.FlowDiffMatchersImpl.IsIpRewrite;
import org.batfish.datamodel.matchers.FlowDiffMatchersImpl.IsPortRewrite;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.PortField;
import org.hamcrest.Matcher;

@ParametersAreNonnullByDefault
public final class FlowDiffMatchers {

  /**
   * Returns a matcher that matches when the {@link FlowDiff} is an IP rewrite with {@code
   * expectedIpField}, with old IP {@code expectedOldIp}, and with new IP {@code expectedNewIp}.
   */
  public static @Nonnull Matcher<FlowDiff> isIpRewrite(
      IpField expectedIpField, Ip expectedOldIp, Ip expectedNewIp) {
    return new IsIpRewrite(
        equalTo(expectedIpField), equalTo(expectedOldIp), equalTo(expectedNewIp));
  }

  /**
   * Returns a matcher that matches when the {@link FlowDiff} is an IP rewrite with {@code
   * expectedIpField} whose old IP is matched by {@code oldIpMatcher} and whose new IP is matched by
   * {@code newIpMatcher}.
   */
  public static @Nonnull Matcher<FlowDiff> isIpRewrite(
      IpField expectedIpField, Matcher<? super Ip> oldIpMatcher, Matcher<? super Ip> newIpMatcher) {
    return new IsIpRewrite(equalTo(expectedIpField), oldIpMatcher, newIpMatcher);
  }

  /**
   * Returns a matcher that matches when the {@link FlowDiff} is a port rewrite with {@code
   * expectedPortField} whose old port is matched by {@code oldPortMatcher} and whose new port is
   * matched by {@code newPortMatcher}.
   */
  public static @Nonnull Matcher<FlowDiff> isPortRewrite(
      PortField expectedPortField,
      Matcher<? super Integer> oldPortMatcher,
      Matcher<? super Integer> newPortMatcher) {
    return new IsPortRewrite(equalTo(expectedPortField), oldPortMatcher, newPortMatcher);
  }

  private FlowDiffMatchers() {}
}
