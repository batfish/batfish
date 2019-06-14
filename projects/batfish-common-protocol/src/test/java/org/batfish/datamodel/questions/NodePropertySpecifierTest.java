package org.batfish.datamodel.questions;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Iterator;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NodePropertySpecifierTest {

  /** */
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testConstructorBadProperty() {
    _thrown.expect(IllegalArgumentException.class);
    new NodePropertySpecifier("ntp");
  }

  @Test
  public void testConstructorBadPropertyInList() {
    _thrown.expect(IllegalArgumentException.class);
    new NodePropertySpecifier(
        ImmutableSet.of(NodePropertySpecifier.NTP_SOURCE_INTERFACE, "dumdum"));
  }

  @Test
  public void getMatchingProperties() {
    // match everything
    assertThat(
        new NodePropertySpecifier("/.*/").getMatchingProperties(),
        equalTo(
            NodePropertySpecifier.JAVA_MAP.keySet().stream()
                .sorted()
                .collect(ImmutableList.toImmutableList())));

    // match the ntp properties
    assertThat(
        new NodePropertySpecifier("/ntp/").getMatchingProperties(),
        equalTo(
            ImmutableList.of(
                NodePropertySpecifier.NTP_SERVERS, NodePropertySpecifier.NTP_SOURCE_INTERFACE)));
  }

  @Test
  public void testMatchingPropertiesSet() {
    Iterator<String> i = NodePropertySpecifier.JAVA_MAP.keySet().iterator();
    String prop1 = i.next();
    String prop2 = i.next();
    Set<String> firstTwoProperties = ImmutableSet.of(prop1, prop2);

    // should match the two properties passed to constructor
    assertThat(
        new NodePropertySpecifier(firstTwoProperties).getMatchingProperties(),
        containsInAnyOrder(prop1, prop2));
  }
}
