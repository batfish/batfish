package org.batfish.minesweeper.utils;

import static org.batfish.minesweeper.utils.PrefixUtils.asNegativeIpWildcards;
import static org.batfish.minesweeper.utils.PrefixUtils.asPositiveIpWildcards;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/** Tests of utility methods from {@link PrefixUtils} */
public class PrefixUtilsTest {

  /** Test that asPositiveIpWildcards handles null */
  @Test
  public void testAsPositiveIpWildcards() {
    assertThat(asPositiveIpWildcards(null), nullValue());
  }

  /** Test that asNegativeIpWildcards handles null */
  @Test
  public void testAsNegativeIpWildcards() {
    assertThat(asNegativeIpWildcards(null), nullValue());
  }
}
