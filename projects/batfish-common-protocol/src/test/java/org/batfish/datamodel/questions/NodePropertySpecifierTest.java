package org.batfish.datamodel.questions;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Iterator;
import org.junit.Test;

public class NodePropertySpecifierTest {

  @Test
  public void getMatchingProperties() {
    // match everything
    assertThat(
        new NodePropertySpecifier(".*").getMatchingProperties().size(),
        equalTo(NodePropertySpecifier.JAVA_MAP.size()));

    // match the ntp properties
    assertThat(new NodePropertySpecifier("ntp.*").getMatchingProperties().size(), equalTo(2));

    // match nothing: ntp
    assertTrue(new NodePropertySpecifier("ntp").getMatchingProperties().isEmpty());
  }

  @Test
  public void testMatchingPropertiesSet() {
    Iterator<String> i = NodePropertySpecifier.JAVA_MAP.keySet().iterator();
    String prop1 = i.next();
    String prop2 = i.next();
    Collection<String> firstTwoProperties = ImmutableList.of(prop1, prop2);

    // should match the two properties passed to constructor
    assertThat(new NodePropertySpecifier(firstTwoProperties).getMatchingProperties(), hasSize(2));

    Collection<String> longer = ImmutableList.of(prop1 + prop1);

    // should not match longer
    assertThat(new NodePropertySpecifier(longer).getMatchingProperties(), emptyIterable());

    Collection<String> shorter = ImmutableList.of(prop1.substring(0, 1));

    // should not match shorter
    assertThat(new NodePropertySpecifier(shorter).getMatchingProperties(), emptyIterable());
  }
}
