package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Optional;
import org.junit.Test;

public class ExpUtilTest {

  @Test
  public void testDefaultValue() {
    // Test all standard aliases
    assertThat(ExpUtil.defaultValue("be"), equalTo(Optional.of(0)));
    assertThat(ExpUtil.defaultValue("be1"), equalTo(Optional.of(1)));
    assertThat(ExpUtil.defaultValue("ef"), equalTo(Optional.of(2)));
    assertThat(ExpUtil.defaultValue("ef1"), equalTo(Optional.of(3)));
    assertThat(ExpUtil.defaultValue("af11"), equalTo(Optional.of(4)));
    assertThat(ExpUtil.defaultValue("af12"), equalTo(Optional.of(5)));
    assertThat(ExpUtil.defaultValue("nc1"), equalTo(Optional.of(6)));
    assertThat(ExpUtil.defaultValue("cs6"), equalTo(Optional.of(6)));
    assertThat(ExpUtil.defaultValue("nc2"), equalTo(Optional.of(7)));
    assertThat(ExpUtil.defaultValue("cs7"), equalTo(Optional.of(7)));

    // Test unknown alias
    assertThat(ExpUtil.defaultValue("unknown"), equalTo(Optional.empty()));
  }
}
