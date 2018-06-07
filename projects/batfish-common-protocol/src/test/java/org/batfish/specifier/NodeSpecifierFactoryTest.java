package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import java.util.regex.Pattern;
import org.junit.Test;

public class NodeSpecifierFactoryTest {
  @Test
  public void testNameRegexNodeSpecifierFactory() {
    assertThat(
        NodeSpecifierFactory.load(NameRegexNodeSpecifierFactory.NAME),
        instanceOf(NameRegexNodeSpecifierFactory.class));
    assertThat(
        NodeSpecifierFactory.load(NameRegexNodeSpecifierFactory.NAME).buildNodeSpecifier("foo"),
        equalTo(new NameRegexNodeSpecifier(Pattern.compile("foo"))));
  }
}
