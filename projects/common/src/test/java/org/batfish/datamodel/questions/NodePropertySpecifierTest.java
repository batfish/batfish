package org.batfish.datamodel.questions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

import com.google.common.collect.ImmutableSet;
import java.util.Iterator;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link NodePropertySpecifier} */
public class NodePropertySpecifierTest {

  /** */
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testConstructorBadPropertyInList() {
    _thrown.expect(IllegalArgumentException.class);
    new NodePropertySpecifier(
        ImmutableSet.of(NodePropertySpecifier.NTP_SOURCE_INTERFACE, "dumdum"));
  }

  @Test
  public void testMatchingPropertiesSet() {
    Iterator<String> i = NodePropertySpecifier.ALL.getMatchingProperties().iterator();
    String prop1 = i.next();
    String prop2 = i.next();
    Set<String> firstTwoProperties = ImmutableSet.of(prop1, prop2);

    // should match the two properties passed to constructor
    assertThat(
        new NodePropertySpecifier(firstTwoProperties).getMatchingProperties(),
        containsInAnyOrder(prop1, prop2));
  }

  @Test
  public void testRegexNoMatch() {
    NodePropertySpecifier spec = NodePropertySpecifier.create("/no-matching-properties/");
    assertThat(spec.getMatchingProperties(), empty());
  }
}
