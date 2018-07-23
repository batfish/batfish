package org.batfish.coordinator.resources;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.stream.Collectors;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.role.addressbook.AddressBook;
import org.batfish.role.addressbook.AddressGroup;
import org.batfish.role.addressbook.ServiceEndpoint;
import org.batfish.role.addressbook.ServiceObject;
import org.batfish.role.addressbook.ServiceObjectGroup;
import org.junit.Test;

public class AddressBookBeanTest {

  private static void beanBookMatch(AddressBookBean bean, AddressBook book) {
    assertThat(bean.name, equalTo(book.getName()));
    assertThat(
        bean.addressGroups,
        equalTo(
            book.getAddressGroups()
                .stream()
                .map(ag -> new AddressGroupBean(ag))
                .collect(Collectors.toSet())));
    assertThat(
        bean.serviceEndpoints,
        equalTo(
            book.getServiceEndpoints()
                .stream()
                .map(se -> new ServiceEndpointBean(se))
                .collect(Collectors.toSet())));
    assertThat(
        bean.serviceObjectGroups,
        equalTo(
            book.getServiceObjectGroups()
                .stream()
                .map(sog -> new ServiceObjectGroupBean(sog))
                .collect(Collectors.toSet())));
    assertThat(
        bean.serviceObjects,
        equalTo(
            book.getServiceObjects()
                .stream()
                .map(so -> new ServiceObjectBean(so))
                .collect(Collectors.toSet())));
  }

  @Test
  public void constructorEmptyBook() {
    AddressBook book1 = new AddressBook(null, "book1", null, null, null);

    beanBookMatch(new AddressBookBean(book1), book1);
  }

  @Test
  public void constructorNonEmptyBook() {
    AddressBook book2 =
        new AddressBook(
            ImmutableList.of(new AddressGroup(ImmutableSortedSet.of(), "ag1")),
            "book2",
            ImmutableList.of(new ServiceEndpoint("ag1", "se1", "so1")),
            ImmutableList.of(new ServiceObjectGroup("sog1", ImmutableSortedSet.of())),
            ImmutableList.of(new ServiceObject(IpProtocol.TCP, "so1", new SubRange(2, 3))));

    beanBookMatch(new AddressBookBean(book2), book2);
  }

  @Test
  public void toAddressBook() {
    AddressBookBean bean =
        new AddressBookBean(
            new AddressBook(
                ImmutableList.of(new AddressGroup(ImmutableSortedSet.of(), "ag1")),
                "book2",
                ImmutableList.of(new ServiceEndpoint("ag1", "se1", "so1")),
                ImmutableList.of(new ServiceObjectGroup("sog1", ImmutableSortedSet.of())),
                ImmutableList.of(new ServiceObject(IpProtocol.TCP, "so1", new SubRange(2, 3)))));

    beanBookMatch(bean, bean.toAddressBook());
  }
}
