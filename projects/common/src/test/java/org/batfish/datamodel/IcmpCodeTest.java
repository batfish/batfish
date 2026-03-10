package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.junit.Test;

public class IcmpCodeTest {
  @Test
  public void getName() {
    assertThat(IcmpCode.of(3, 7).getName(), equalTo(Optional.of("DESTINATION-HOST-UNKNOWN")));
    assertThat(IcmpCode.of(55, 55).getName(), equalTo(Optional.empty()));
  }

  @Test
  public void namesDontHaveUnderscores() {
    for (IcmpCode code : IcmpCode.ALL) {
      assertTrue(code.getName().isPresent());
      assertFalse(code.toString(), code.getName().get().contains("_"));
    }
  }

  @Test
  public void allCodesAreUnique() {
    long distinctTypesAndCodes =
        IcmpCode.ALL.stream().mapToInt(c -> (c.getType() << 8) + c.getCode()).distinct().count();
    assertThat(IcmpCode.ALL, hasSize((int) distinctTypesAndCodes));
  }
}
