package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.ADDRESS_GROUP_NAME;
import static org.batfish.specifier.parboiled.Anchor.Type.STRING_LITERAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class PotentialMatchTest {

  @Test
  public void testGetMatchNonStringLiteral() {
    PotentialMatch pm =
        new PotentialMatch(
            new PathElement(ADDRESS_GROUP_NAME, "\"label\"", 0, 0), "pfx", ImmutableList.of());
    assertThat(pm.getMatch(), equalTo(null));
  }

  @Test
  public void testGetMatchStringLiteral() {
    PotentialMatch pm =
        new PotentialMatch(
            new PathElement(STRING_LITERAL, "\"label\"", 0, 0), "pfx", ImmutableList.of());
    assertThat(pm.getMatch(), equalTo("label"));
  }
}
