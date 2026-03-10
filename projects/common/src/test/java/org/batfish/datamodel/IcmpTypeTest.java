package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;

import java.util.Optional;
import org.junit.Test;

public class IcmpTypeTest {
  @Test
  public void getName() {
    assertThat(IcmpType.getName(8), equalTo(Optional.of("ECHO-REQUEST")));
    assertThat(IcmpType.getName(75), equalTo(Optional.empty()));
  }

  @Test
  public void namesDontHaveUnderscores() {
    for (String desc : IcmpType.NAMES.values()) {
      assertFalse(desc, desc.contains("_"));
    }
  }
}
