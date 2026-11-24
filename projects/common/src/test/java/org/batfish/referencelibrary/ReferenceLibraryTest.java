package org.batfish.referencelibrary;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.referencelibrary.ReferenceLibrary.checkDuplicates;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.SortedSet;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Tests for {@link ReferenceLibrary} */
class ReferenceLibraryTest {

  /** check that we barf on duplicates */
  @Test
  void testCheckDuplicatesDups() {
    Throwable exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> checkDuplicates("test", ImmutableList.of("a", "b", "a")));
    assertThat(exception.getMessage(), containsString("Duplicate"));
  }

  /** check that we do NOT barf on duplicates */
  @Test
  void testCheckDuplicatesNoDups() {
    checkDuplicates("test", ImmutableList.of(""));
    checkDuplicates("test", ImmutableList.of("a"));
    checkDuplicates("test", ImmutableList.of("a", "b", "c"));
  }

  /** check that we deserialize successfully and into two books */
  @Test
  void testLibraryDeserialization() throws IOException {
    ReferenceLibrary library =
        BatfishObjectMapper.mapper()
            .readValue(
                readResource("org/batfish/referencelibrary/libraryTwoBooks.json", UTF_8),
                ReferenceLibrary.class);

    assertThat(library.getReferenceBooks(), hasSize(2));
  }

  /** check that we barf on duplicate book names */
  @Test
  void testLibraryDeserializationDuplicateBooks() {
    Throwable exception =
        assertThrows(
            ValueInstantiationException.class,
            () ->
                BatfishObjectMapper.mapper()
                    .readValue(
                        readResource(
                            "org/batfish/referencelibrary/libraryDuplicateBooks.json", UTF_8),
                        ReferenceLibrary.class));
    assertThat(exception.getMessage(), containsString("Duplicate"));
  }

  /** check that merger of reference books is proper */
  @Test
  void testMergeReferenceBooks() throws IOException {
    ReferenceLibrary library =
        new ReferenceLibrary(
            ImmutableList.of(
                ReferenceBook.builder("book1").build(), ReferenceBook.builder("book2").build()));

    SortedSet<ReferenceBook> newBooks =
        ImmutableSortedSet.of(
            ReferenceBook.builder("book1")
                .setAddressGroups(
                    ImmutableList.of(new AddressGroup(ImmutableSortedSet.of(), "add1")))
                .build(),
            ReferenceBook.builder("book3").build());

    ReferenceLibrary merged = library.mergeReferenceBooks(newBooks);

    assertThat(
        merged.getReferenceBooks(),
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
