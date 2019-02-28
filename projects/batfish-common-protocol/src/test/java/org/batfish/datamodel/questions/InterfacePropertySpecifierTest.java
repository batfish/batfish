package org.batfish.datamodel.questions;

import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Iterator;
import org.junit.Test;

public class InterfacePropertySpecifierTest {

  @Test
  public void getMatchingProperties() {
    // match everything
    assertThat(
        new InterfacePropertySpecifier(".*").getMatchingProperties().size(),
        equalTo(InterfacePropertySpecifier.JAVA_MAP.size()));

    // match the description
    assertThat(new InterfacePropertySpecifier("desc.*").getMatchingProperties().size(), equalTo(1));

    // match nothing: ntp
    assertTrue(new InterfacePropertySpecifier("ntp").getMatchingProperties().isEmpty());
  }

  @Test
  public void testMatchingPropertiesSet() {
    Iterator<String> i = InterfacePropertySpecifier.JAVA_MAP.keySet().iterator();
    String prop1 = i.next();
    String prop2 = i.next();
    Collection<String> firstTwoProperties = ImmutableList.of(prop1, prop2);

    // should match the two properties passed to constructor
    assertThat(
        new InterfacePropertySpecifier(firstTwoProperties).getMatchingProperties(), hasSize(2));

    Collection<String> longer = ImmutableList.of(prop1 + prop1);

    // should not match longer
    assertThat(new InterfacePropertySpecifier(longer).getMatchingProperties(), emptyIterable());

    Collection<String> shorter = ImmutableList.of(prop1.substring(0, 1));

    // should not match shorter
    assertThat(new InterfacePropertySpecifier(shorter).getMatchingProperties(), emptyIterable());
  }

  /**
   * Right now additional arp ips are not supported in interface properties. When it is supported,
   * this test case can be removed
   */
  @Test
  public void testNoAdditionalArpIps() {
    assertThat(
        InterfacePropertySpecifier.JAVA_MAP.keySet(),
        not(hasItem(InterfacePropertySpecifier.ADDITIONAL_ARP_IPS)));
  }
}
