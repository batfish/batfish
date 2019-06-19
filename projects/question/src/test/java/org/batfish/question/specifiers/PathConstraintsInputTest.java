package org.batfish.question.specifiers;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/** Tests of {@link PathConstraintsInput} */
public class PathConstraintsInputTest {
  @Test
  public void testDefaults() {
    PathConstraintsInput pci = PathConstraintsInput.unconstrained();
    assertThat(pci.getStartLocation(), nullValue());
    assertThat(pci.getEndLocation(), nullValue());
    assertThat(pci.getTransitLocations(), nullValue());
    assertThat(pci.getForbiddenLocations(), nullValue());
  }
}
