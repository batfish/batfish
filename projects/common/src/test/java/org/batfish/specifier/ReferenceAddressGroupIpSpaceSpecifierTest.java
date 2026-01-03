package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.ReferenceBook;
import org.junit.Test;

public class ReferenceAddressGroupIpSpaceSpecifierTest {

  @Test
  public void testResolve() {
    ReferenceBook book =
        ReferenceBook.builder("book1")
            .setAddressGroups(
                ImmutableList.of(
                    new AddressGroup(
                        ImmutableSortedSet.of("1.1.1.1", "2.2.2.2:0.0.0.8"), "group1")))
            .build();
    MockSpecifierContext ctxt =
        MockSpecifierContext.builder().setReferenceBooks(ImmutableSortedSet.of(book)).build();

    ReferenceAddressGroupIpSpaceSpecifier specifier =
        new ReferenceAddressGroupIpSpaceSpecifier("group1", "book1");

    assertThat(
        specifier.resolve(ctxt),
        equalTo(
            AclIpSpace.union(
                IpWildcard.parse("1.1.1.1").toIpSpace(),
                IpWildcard.parse("2.2.2.2:0.0.0.8").toIpSpace())));
  }
}
