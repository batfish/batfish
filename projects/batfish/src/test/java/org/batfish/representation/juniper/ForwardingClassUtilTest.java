package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Optional;
import org.junit.Test;

public class ForwardingClassUtilTest {

  @Test
  public void testBuiltinForwardingClasses() {
    // Test all built-in forwarding classes
    assertThat(ForwardingClassUtil.isBuiltin("best-effort"), equalTo(true));
    assertThat(ForwardingClassUtil.defaultQueueNumber("best-effort"), equalTo(Optional.of(0)));

    assertThat(ForwardingClassUtil.isBuiltin("expedited-forwarding"), equalTo(true));
    assertThat(
        ForwardingClassUtil.defaultQueueNumber("expedited-forwarding"), equalTo(Optional.of(1)));

    assertThat(ForwardingClassUtil.isBuiltin("assured-forwarding"), equalTo(true));
    assertThat(
        ForwardingClassUtil.defaultQueueNumber("assured-forwarding"), equalTo(Optional.of(2)));

    assertThat(ForwardingClassUtil.isBuiltin("network-control"), equalTo(true));
    assertThat(ForwardingClassUtil.defaultQueueNumber("network-control"), equalTo(Optional.of(3)));

    // Test standard abbreviations
    assertThat(ForwardingClassUtil.isBuiltin("be"), equalTo(true));
    assertThat(ForwardingClassUtil.defaultQueueNumber("be"), equalTo(Optional.of(0)));

    assertThat(ForwardingClassUtil.isBuiltin("ef"), equalTo(true));
    assertThat(ForwardingClassUtil.defaultQueueNumber("ef"), equalTo(Optional.of(1)));

    assertThat(ForwardingClassUtil.isBuiltin("af"), equalTo(true));
    assertThat(ForwardingClassUtil.defaultQueueNumber("af"), equalTo(Optional.of(2)));

    assertThat(ForwardingClassUtil.isBuiltin("nc"), equalTo(true));
    assertThat(ForwardingClassUtil.defaultQueueNumber("nc"), equalTo(Optional.of(3)));

    // Test custom forwarding classes
    assertThat(ForwardingClassUtil.isBuiltin("custom-class"), equalTo(false));
    assertThat(ForwardingClassUtil.defaultQueueNumber("custom-class"), equalTo(Optional.empty()));

    assertThat(ForwardingClassUtil.isBuiltin("FC-CLASS1"), equalTo(false));
    assertThat(ForwardingClassUtil.defaultQueueNumber("FC-CLASS1"), equalTo(Optional.empty()));
  }
}
