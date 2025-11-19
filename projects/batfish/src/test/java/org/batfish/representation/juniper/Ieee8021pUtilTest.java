package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Optional;
import org.junit.Test;

public class Ieee8021pUtilTest {

  @Test
  public void testDefaultValue() {
    // Test all standard aliases
    assertThat(Ieee8021pUtil.defaultValue("be"), equalTo(Optional.of(0)));
    assertThat(Ieee8021pUtil.defaultValue("be1"), equalTo(Optional.of(1)));
    assertThat(Ieee8021pUtil.defaultValue("ef"), equalTo(Optional.of(2)));
    assertThat(Ieee8021pUtil.defaultValue("ef1"), equalTo(Optional.of(3)));
    assertThat(Ieee8021pUtil.defaultValue("af11"), equalTo(Optional.of(4)));
    assertThat(Ieee8021pUtil.defaultValue("af12"), equalTo(Optional.of(5)));
    assertThat(Ieee8021pUtil.defaultValue("nc1"), equalTo(Optional.of(6)));
    assertThat(Ieee8021pUtil.defaultValue("cs6"), equalTo(Optional.of(6)));
    assertThat(Ieee8021pUtil.defaultValue("nc2"), equalTo(Optional.of(7)));
    assertThat(Ieee8021pUtil.defaultValue("cs7"), equalTo(Optional.of(7)));

    // Test unknown alias
    assertThat(Ieee8021pUtil.defaultValue("unknown"), equalTo(Optional.empty()));
  }
}
