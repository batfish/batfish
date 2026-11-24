package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class IcmpCodeTest {
  @Test
  void getName() {
    assertThat(IcmpCode.of(3, 7).getName(), equalTo(Optional.of("DESTINATION-HOST-UNKNOWN")));
    assertThat(IcmpCode.of(55, 55).getName(), equalTo(Optional.empty()));
  }

  @Test
  void namesDontHaveUnderscores() {
    for (IcmpCode code : IcmpCode.ALL) {
      assertTrue(code.getName().isPresent());
      assertFalse(code.getName().get().contains("_"), code.toString());
    }
  }

  @Test
  void allCodesAreUnique() {
    long distinctTypesAndCodes =
        IcmpCode.ALL.stream().mapToInt(c -> (c.getType() << 8) + c.getCode()).distinct().count();
    assertThat(IcmpCode.ALL, hasSize((int) distinctTypesAndCodes));
  }
}
