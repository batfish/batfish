package org.batfish.datamodel.questions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableSet;
import java.util.Iterator;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Tests for {@link NodePropertySpecifier} */
class NodePropertySpecifierTest {

  @Test
  void testConstructorBadPropertyInList() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          new NodePropertySpecifier(
              ImmutableSet.of(NodePropertySpecifier.NTP_SOURCE_INTERFACE, "dumdum"));
        });
  }

  @Test
  void testMatchingPropertiesSet() {
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
  void testRegexNoMatch() {
    NodePropertySpecifier spec = NodePropertySpecifier.create("/no-matching-properties/");
    assertThat(spec.getMatchingProperties(), empty());
  }
}
