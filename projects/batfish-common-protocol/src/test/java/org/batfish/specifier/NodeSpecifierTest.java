package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import java.util.regex.Pattern;
import org.junit.Test;

public class NodeSpecifierTest {
  @Test
  public void testNameRegexNodeSpecifier() {
    NameRegexNodeSpecifier specifier = new NameRegexNodeSpecifier(Pattern.compile("hey"));
    assertThat(specifier, equalTo(specifier));
    assertThat(specifier, not(equalTo(AllNodesNodeSpecifier.INSTANCE)));
  }
}
