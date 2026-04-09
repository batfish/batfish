package org.batfish.datamodel.matchers;

import static org.hamcrest.Matchers.equalTo;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.FlowDiff;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.PortField;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

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

  private static final class IsIpRewrite extends TypeSafeDiagnosingMatcher<FlowDiff> {

    private final @Nonnull Matcher<? super IpField> _ipFieldMatcher;
    private final @Nonnull Matcher<? super Ip> _newIpMatcher;
    private final @Nonnull Matcher<? super Ip> _oldIpMatcher;

    public IsIpRewrite(
        Matcher<IpField> ipFieldMatcher,
        Matcher<? super Ip> oldIpMatcher,
        Matcher<? super Ip> newIpMatcher) {
      _ipFieldMatcher = ipFieldMatcher;
      _oldIpMatcher = oldIpMatcher;
      _newIpMatcher = newIpMatcher;
    }

    @Override
    public void describeTo(Description description) {
      description
          .appendText("An IP rewrite with ipField:")
          .appendDescriptionOf(_ipFieldMatcher)
          .appendText("and old IP:")
          .appendDescriptionOf(_oldIpMatcher)
          .appendText("and new IP:")
          .appendDescriptionOf(_newIpMatcher);
    }

    @Override
    protected boolean matchesSafely(FlowDiff item, Description mismatchDescription) {
      IpField ipField = item.getIpField();
      if (ipField == null) {
        mismatchDescription.appendText("Not an IP rewrite");
        return false;
      }
      if (!_ipFieldMatcher.matches(ipField)) {
        _ipFieldMatcher.describeMismatch(ipField, mismatchDescription);
        return false;
      }
      // assume old and new fields are parseable as IPs if this is an IP rewrite
      Ip oldIp = Ip.parse(item.getOldValue());
      if (!_oldIpMatcher.matches(oldIp)) {
        _oldIpMatcher.describeMismatch(oldIp, mismatchDescription);
        return false;
      }
      Ip newIp = Ip.parse(item.getNewValue());
      if (!_newIpMatcher.matches(newIp)) {
        _newIpMatcher.describeMismatch(newIp, mismatchDescription);
        return false;
      }
      return true;
    }
  }

  private static final class IsPortRewrite extends TypeSafeDiagnosingMatcher<FlowDiff> {

    private final @Nonnull Matcher<? super Integer> _newPortMatcher;
    private final @Nonnull Matcher<? super Integer> _oldPortMatcher;
    private final @Nonnull Matcher<? super PortField> _portFieldMatcher;

    public IsPortRewrite(
        Matcher<PortField> portFieldMatcher,
        Matcher<? super Integer> oldPortMatcher,
        Matcher<? super Integer> newPortMatcher) {
      _portFieldMatcher = portFieldMatcher;
      _oldPortMatcher = oldPortMatcher;
      _newPortMatcher = newPortMatcher;
    }

    @Override
    public void describeTo(Description description) {
      description
          .appendText("A port rewrite with portField:")
          .appendDescriptionOf(_portFieldMatcher)
          .appendText("and old port:")
          .appendDescriptionOf(_oldPortMatcher)
          .appendText("and new port:")
          .appendDescriptionOf(_newPortMatcher);
    }

    @Override
    protected boolean matchesSafely(FlowDiff item, Description mismatchDescription) {
      PortField portField = item.getPortField();
      if (portField == null) {
        mismatchDescription.appendText("Not a port rewrite");
        return false;
      }
      if (!_portFieldMatcher.matches(portField)) {
        _portFieldMatcher.describeMismatch(portField, mismatchDescription);
        return false;
      }
      // assume old and new fields are parseable as integers if this is a port rewrite
      Integer oldPort = Integer.parseInt(item.getOldValue());
      if (!_oldPortMatcher.matches(oldPort)) {
        _oldPortMatcher.describeMismatch(oldPort, mismatchDescription);
        return false;
      }
      Integer newPort = Integer.parseInt(item.getNewValue());
      if (!_newPortMatcher.matches(newPort)) {
        _newPortMatcher.describeMismatch(newPort, mismatchDescription);
        return false;
      }
      return true;
    }
  }
}
