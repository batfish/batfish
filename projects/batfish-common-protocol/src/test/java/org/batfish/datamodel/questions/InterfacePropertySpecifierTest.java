package org.batfish.datamodel.questions;

import static org.batfish.datamodel.questions.InterfacePropertySpecifier.INCOMING_FILTER_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Iterator;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.TestInterface;
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
    new InterfacePropertySpecifier(ImmutableSet.of("ntp"));
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
        InterfacePropertySpecifier.create("/.*/").getMatchingProperties(),
        equalTo(InterfacePropertySpecifier.ALL.getMatchingProperties()));

    // match the description
    assertThat(
        InterfacePropertySpecifier.create("/desc.*/").getMatchingProperties(),
        equalTo(ImmutableList.of(InterfacePropertySpecifier.DESCRIPTION)));
  }

  @Test
  public void testMatchingPropertiesSet() {
    Iterator<String> i = InterfacePropertySpecifier.ALL.getMatchingProperties().iterator();
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
    Configuration c = new Configuration("hostname", ConfigurationFormat.CISCO_IOS);
    IpAccessList acl =
        IpAccessList.builder()
            .setOwner(c)
            .setName("MY_ACL")
            .setLines(ImmutableList.of(ExprAclLine.ACCEPT_ALL))
            .build();
    Interface i1 = TestInterface.builder().setOwner(c).setName("i1").setIncomingFilter(acl).build();
    assertThat(
        InterfacePropertySpecifier.getPropertyDescriptor(INCOMING_FILTER_NAME)
            .getGetter()
            .apply(i1),
        equalTo(acl.getName()));
  }

  @Test
  public void testInterfaceType() {
    assertThat(
        InterfacePropertySpecifier.create(InterfacePropertySpecifier.INTERFACE_TYPE)
            .getMatchingProperties(),
        contains(InterfacePropertySpecifier.INTERFACE_TYPE));
  }

  @Test
  public void testDefault() {
    assertThat(
        InterfacePropertySpecifier.create(null).getMatchingProperties(),
        not(hasItem(InterfacePropertySpecifier.INTERFACE_TYPE)));
  }
}
