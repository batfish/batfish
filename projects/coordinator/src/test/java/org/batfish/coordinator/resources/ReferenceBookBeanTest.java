package org.batfish.coordinator.resources;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.stream.Collectors;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ServiceEndpoint;
import org.batfish.referencelibrary.ServiceObject;
import org.batfish.referencelibrary.ServiceObjectGroup;
import org.junit.Test;

public class ReferenceBookBeanTest {

  private static void beanBookMatch(ReferenceBookBean bean, ReferenceBook book) {
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
    ReferenceBook book1 = new ReferenceBook(null, "book1", null, null, null);

    beanBookMatch(new ReferenceBookBean(book1), book1);
  }

  @Test
  public void constructorNonEmptyBook() {
    ReferenceBook book2 =
        new ReferenceBook(
            ImmutableList.of(new AddressGroup(ImmutableSortedSet.of(), "ag1")),
            "book2",
            ImmutableList.of(new ServiceEndpoint("ag1", "se1", "so1")),
            ImmutableList.of(new ServiceObjectGroup("sog1", ImmutableSortedSet.of())),
            ImmutableList.of(new ServiceObject(IpProtocol.TCP, "so1", new SubRange(2, 3))));

    beanBookMatch(new ReferenceBookBean(book2), book2);
  }

  @Test
  public void toAddressBook() {
    ReferenceBookBean bean =
        new ReferenceBookBean(
            new ReferenceBook(
                ImmutableList.of(new AddressGroup(ImmutableSortedSet.of(), "ag1")),
                "book2",
                ImmutableList.of(new ServiceEndpoint("ag1", "se1", "so1")),
                ImmutableList.of(new ServiceObjectGroup("sog1", ImmutableSortedSet.of())),
                ImmutableList.of(new ServiceObject(IpProtocol.TCP, "so1", new SubRange(2, 3)))));

    beanBookMatch(bean, bean.toAddressBook());
  }
}
