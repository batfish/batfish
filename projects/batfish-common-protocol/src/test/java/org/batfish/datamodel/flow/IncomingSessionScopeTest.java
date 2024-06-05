package org.batfish.datamodel.flow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class IncomingSessionScopeTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new IncomingSessionScope(ImmutableSet.of("a")),
            new IncomingSessionScope(ImmutableSet.of("a")))
        .addEqualityGroup(new IncomingSessionScope(ImmutableSet.of("b")))
        .testEquals();
  }

  @Test
  public void testSerialization() {
    IncomingSessionScope scope = new IncomingSessionScope(ImmutableSet.of("a"));
    assertThat(BatfishObjectMapper.clone(scope, SessionScope.class), equalTo(scope));
  }
}
