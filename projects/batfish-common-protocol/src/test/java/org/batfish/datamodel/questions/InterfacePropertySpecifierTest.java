package org.batfish.datamodel.questions;

import static org.batfish.datamodel.questions.InterfacePropertySpecifier.INCOMING_FILTER_NAME;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Iterator;
import java.util.Set;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link InterfacePropertySpecifier} */
public class InterfacePropertySpecifierTest {

  /** */
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testConstructorBadProperty() {
    _thrown.expect(IllegalArgumentException.class);
    new InterfacePropertySpecifier("ntp");
  }

  @Test
  public void testConstructorBadPropertyInList() {
    _thrown.expect(IllegalArgumentException.class);
    new InterfacePropertySpecifier(ImmutableSet.of(INCOMING_FILTER_NAME, "dumdum"));
  }

  @Test
  public void getMatchingProperties() {
    // match everything
    assertThat(
        new InterfacePropertySpecifier("/.*/").getMatchingProperties(),
        equalTo(
            InterfacePropertySpecifier.JAVA_MAP.keySet().stream()
                .sorted()
                .collect(ImmutableList.toImmutableList())));

    // match the description
    assertThat(
        new InterfacePropertySpecifier("/desc.*/").getMatchingProperties(),
        equalTo(ImmutableList.of(InterfacePropertySpecifier.DESCRIPTION)));
  }

  @Test
  public void testMatchingPropertiesSet() {
    Iterator<String> i = InterfacePropertySpecifier.JAVA_MAP.keySet().iterator();
    String prop1 = i.next();
    String prop2 = i.next();
    Set<String> firstTwoProperties = ImmutableSet.of(prop1, prop2);

    // should match the two properties passed to constructor
    assertThat(
        new InterfacePropertySpecifier(firstTwoProperties).getMatchingProperties(),
        equalTo(ImmutableList.of(prop1, prop2)));
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
