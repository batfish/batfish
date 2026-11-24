package org.batfish.question.specifiers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;

/** Tests of {@link PathConstraintsInput} */
class PathConstraintsInputTest {
  @Test
  void testDefaults() {
    PathConstraintsInput pci = PathConstraintsInput.unconstrained();
    assertThat(pci.getStartLocation(), nullValue());
    assertThat(pci.getEndLocation(), nullValue());
    assertThat(pci.getTransitLocations(), nullValue());
    assertThat(pci.getForbiddenLocations(), nullValue());
  }
}
