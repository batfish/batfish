package org.batfish.coordinator.resources;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import org.batfish.role.addressbook.AddressBook;
import org.batfish.role.addressbook.AddressLibrary;
import org.junit.Test;

public class AddressLibraryBeanTest {

  /** Test if empty library is bean'd properly */
  @Test
  public void constructorEmpty() throws IOException {
    AddressLibrary library = new AddressLibrary(ImmutableList.of());

    assertThat(new AddressLibraryBean(library).books, equalTo(ImmutableSortedSet.of()));
  }

  /** Test if a non-empty library is bean'd properly */
  @Test
  public void constructorNonEmpty() throws IOException {
    AddressLibrary library =
        new AddressLibrary(
            ImmutableList.of(
                new AddressBook(null, "book1", null, null, null),
                new AddressBook(null, "book2", null, null, null)));

    assertThat(
        new AddressLibraryBean(library).books,
        equalTo(
            ImmutableSet.of(
                new AddressBookBean(new AddressBook(null, "book1", null, null, null)),
                new AddressBookBean(new AddressBook(null, "book2", null, null, null)))));
  }
}
