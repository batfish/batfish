package org.batfish.role.addressbook;

import static org.batfish.role.addressbook.AddressLibrary.checkDuplicates;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.SortedSet;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AddressLibraryTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  /** check that we barf on duplicates */
  @Test
  public void checkDuplicatesDups() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Duplicate");

    checkDuplicates("test", ImmutableList.of("a", "b", "a"));
  }

  /** check that we do NOT barf on duplicates */
  @Test
  public void checkDuplicatesNoDups() {
    checkDuplicates("test", ImmutableList.of(""));
    checkDuplicates("test", ImmutableList.of("a"));
    checkDuplicates("test", ImmutableList.of("a", "b", "c"));
  }

  /** check that we deserialize successfully and into two books */
  @Test
  public void libraryDeserialization() throws IOException {
    AddressLibrary library =
        BatfishObjectMapper.mapper()
            .readValue(
                CommonUtil.readResource("org/batfish/role/addressbook/libraryTwoBooks.json"),
                AddressLibrary.class);

    assertThat(library.getAddressBooks(), hasSize(2));
  }

  /** check that we barf on duplicate book names */
  @Test
  public void libraryDeserializationDuplicateBooks() throws IOException {
    _thrown.expect(InvalidDefinitionException.class);
    _thrown.expectMessage("Duplicate");

    BatfishObjectMapper.mapper()
        .readValue(
            CommonUtil.readResource("org/batfish/role/addressbook/libraryDuplicateBooks.json"),
            AddressLibrary.class);
  }

  /** check that merger of address books is proper */
  @Test
  public void mergeAddressBooks() throws IOException {
    Path tempPath = CommonUtil.createTempFile("addresslibrary", "tmp");
    AddressLibrary library =
        new AddressLibrary(
            ImmutableList.of(
                new AddressBook(null, "book1", null, null, null),
                new AddressBook(null, "book2", null, null, null)));
    AddressLibrary.write(library, tempPath);

    SortedSet<AddressBook> newBooks =
        ImmutableSortedSet.of(
            new AddressBook(
                ImmutableList.of(new AddressGroup(ImmutableSortedSet.of(), "add1")),
                "book1",
                null,
                null,
                null),
            new AddressBook(null, "book3", null, null, null));

    AddressLibrary.mergeAddressBooks(tempPath, newBooks);

    assertThat(
        AddressLibrary.read(tempPath).getAddressBooks(),
        equalTo(
            ImmutableSortedSet.of(
                new AddressBook(
                    ImmutableList.of(new AddressGroup(ImmutableSortedSet.of(), "add1")),
                    "book1",
                    null,
                    null,
                    null),
                new AddressBook(null, "book2", null, null, null),
                new AddressBook(null, "book3", null, null, null))));
  }
}
