package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Optional;
import org.junit.Test;

public class InetPrecedenceUtilTest {

  @Test
  public void testDefaultValue() {
    // Test all standard aliases
    assertThat(InetPrecedenceUtil.defaultValue("be"), equalTo(Optional.of(0)));
    assertThat(InetPrecedenceUtil.defaultValue("be1"), equalTo(Optional.of(1)));
    assertThat(InetPrecedenceUtil.defaultValue("ef"), equalTo(Optional.of(2)));
    assertThat(InetPrecedenceUtil.defaultValue("ef1"), equalTo(Optional.of(3)));
    assertThat(InetPrecedenceUtil.defaultValue("af11"), equalTo(Optional.of(4)));
    assertThat(InetPrecedenceUtil.defaultValue("af12"), equalTo(Optional.of(5)));
    assertThat(InetPrecedenceUtil.defaultValue("nc1"), equalTo(Optional.of(6)));
    assertThat(InetPrecedenceUtil.defaultValue("cs6"), equalTo(Optional.of(6)));
    assertThat(InetPrecedenceUtil.defaultValue("nc2"), equalTo(Optional.of(7)));
    assertThat(InetPrecedenceUtil.defaultValue("cs7"), equalTo(Optional.of(7)));

    // Test unknown alias
    assertThat(InetPrecedenceUtil.defaultValue("unknown"), equalTo(Optional.empty()));
  }
}
