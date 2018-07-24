package org.batfish.specifier;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.stream.Collectors;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.role.addressbook.AddressBook;
import org.batfish.role.addressbook.AddressGroup;
import org.junit.Test;

public class AddressBookIpSpaceSpecifierTest {

  @Test
  public void resolve() {
    AddressBook book =
        new AddressBook(
            ImmutableList.of(
                new AddressGroup(ImmutableSortedSet.of("1.1.1.1", "2.2.2.2:0.0.0.8"), "group1")),
            "book1",
            null,
            null,
            null);
    MockSpecifierContext ctxt =
        MockSpecifierContext.builder().setAddressBooks(ImmutableSortedSet.of(book)).build();

    AddressBookIpSpaceSpecifier specifier = new AddressBookIpSpaceSpecifier("group1", "book1");
    IpSpace resolvedSpace =
        AclIpSpace.union(
            specifier
                .resolve(ImmutableSet.of(), ctxt)
                .getEntries()
                .stream()
                .map(e -> e.getIpSpace())
                .collect(Collectors.toSet()));

    assertThat(
        resolvedSpace,
        equalTo(
            AclIpSpace.union(
                new IpWildcard("1.1.1.1").toIpSpace(),
                new IpWildcard("2.2.2.2:0.0.0.8").toIpSpace())));
  }
}
