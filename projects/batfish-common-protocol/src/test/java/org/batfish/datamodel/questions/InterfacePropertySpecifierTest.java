package org.batfish.datamodel.questions;

import static org.batfish.datamodel.questions.InterfacePropertySpecifier.INCOMING_FILTER_NAME;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Iterator;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
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

  @Test
  public void testIncomingFilterReturnsName() {
    IpAccessList acl =
        IpAccessList.builder()
            .setName("MY_ACL")
            .setLines(ImmutableList.of(IpAccessListLine.ACCEPT_ALL))
            .build();
    Interface i1 = Interface.builder().setName("i1").setIncomingFilter(acl).build();
    i1.setInboundFilterName(acl.getName());
    assertThat(
        InterfacePropertySpecifier.JAVA_MAP.get(INCOMING_FILTER_NAME).getGetter().apply(i1),
        equalTo(acl.getName()));
  }
}
