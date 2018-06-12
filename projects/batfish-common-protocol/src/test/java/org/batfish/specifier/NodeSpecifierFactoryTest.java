package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import java.util.regex.Pattern;
import org.batfish.common.BatfishException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NodeSpecifierFactoryTest {
  @Rule public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testNameRegexNodeSpecifierFactory() {
    assertThat(
        NodeSpecifierFactory.load(NameRegexNodeSpecifierFactory.NAME),
        instanceOf(NameRegexNodeSpecifierFactory.class));
    assertThat(
        NodeSpecifierFactory.load(NameRegexNodeSpecifierFactory.NAME).buildNodeSpecifier("foo"),
        equalTo(new NameRegexNodeSpecifier(Pattern.compile("foo"))));
  }

  @Test
  public void testUnknownNodeSpecifierFactory() {
    exception.expect(BatfishException.class);
    NodeSpecifierFactory.load("");
  }
}
