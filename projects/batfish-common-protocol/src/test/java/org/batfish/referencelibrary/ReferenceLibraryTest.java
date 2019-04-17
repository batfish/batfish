package org.batfish.referencelibrary;

import static org.batfish.referencelibrary.ReferenceLibrary.checkDuplicates;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

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

/** Tests for {@link ReferenceLibrary} */
public class ReferenceLibraryTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  /** check that we barf on duplicates */
  @Test
  public void testCheckDuplicatesDups() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Duplicate");

    checkDuplicates("test", ImmutableList.of("a", "b", "a"));
  }

  /** check that we do NOT barf on duplicates */
  @Test
  public void testCheckDuplicatesNoDups() {
    checkDuplicates("test", ImmutableList.of(""));
    checkDuplicates("test", ImmutableList.of("a"));
    checkDuplicates("test", ImmutableList.of("a", "b", "c"));
  }

  /** check that we deserialize successfully and into two books */
  @Test
  public void testLibraryDeserialization() throws IOException {
    ReferenceLibrary library =
        BatfishObjectMapper.mapper()
            .readValue(
                CommonUtil.readResource("org/batfish/referencelibrary/libraryTwoBooks.json"),
                ReferenceLibrary.class);

    assertThat(library.getReferenceBooks(), hasSize(2));
  }

  /** check that we barf on duplicate book names */
  @Test
  public void testLibraryDeserializationDuplicateBooks() throws IOException {
    _thrown.expect(InvalidDefinitionException.class);
    _thrown.expectMessage("Duplicate");

    BatfishObjectMapper.mapper()
        .readValue(
            CommonUtil.readResource("org/batfish/referencelibrary/libraryDuplicateBooks.json"),
            ReferenceLibrary.class);
  }

  /** check that merger of reference books is proper */
  @Test
  public void testMergeReferenceBooks() throws IOException {
    Path tempPath = CommonUtil.createTempFile("referencelibrary", "tmp");
    ReferenceLibrary library =
        new ReferenceLibrary(
            ImmutableList.of(
                ReferenceBook.builder("book1").build(), ReferenceBook.builder("book2").build()));
    ReferenceLibrary.write(library, tempPath);

    SortedSet<ReferenceBook> newBooks =
        ImmutableSortedSet.of(
            ReferenceBook.builder("book1")
                .setAddressGroups(
                    ImmutableList.of(new AddressGroup(ImmutableSortedSet.of(), "add1")))
                .build(),
            ReferenceBook.builder("book3").build());

    ReferenceLibrary.mergeReferenceBooks(tempPath, newBooks);

    assertThat(
        ReferenceLibrary.read(tempPath).getReferenceBooks(),
        equalTo(
            ImmutableSortedSet.of(
                ReferenceBook.builder("book1")
                    .setAddressGroups(
                        ImmutableList.of(new AddressGroup(ImmutableSortedSet.of(), "add1")))
                    .build(),
                ReferenceBook.builder("book2").build(),
                ReferenceBook.builder("book3").build())));
  }
}
