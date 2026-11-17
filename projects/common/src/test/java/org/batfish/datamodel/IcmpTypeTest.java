package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class IcmpTypeTest {
  @Test
  void getName() {
    assertThat(IcmpType.getName(8), equalTo(Optional.of("ECHO-REQUEST")));
    assertThat(IcmpType.getName(75), equalTo(Optional.empty()));
  }

  @Test
  void namesDontHaveUnderscores() {
    for (String desc : IcmpType.NAMES.values()) {
      assertFalse(desc.contains("_"), desc);
    }
  }
}
