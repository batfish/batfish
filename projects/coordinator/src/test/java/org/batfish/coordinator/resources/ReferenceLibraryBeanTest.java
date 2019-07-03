package org.batfish.coordinator.resources;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.junit.Test;

public class ReferenceLibraryBeanTest {

  /** Test if empty library is bean'd properly */
  @Test
  public void constructorEmpty() {
    ReferenceLibrary library = new ReferenceLibrary(ImmutableList.of());

    assertThat(new ReferenceLibraryBean(library).books, equalTo(ImmutableSortedSet.of()));
  }

  /** Test if a non-empty library is bean'd properly */
  @Test
  public void constructorNonEmpty() {
    ReferenceLibrary library =
        new ReferenceLibrary(
            ImmutableList.of(
                ReferenceBook.builder("book1").build(), ReferenceBook.builder("book2").build()));

    assertThat(
        new ReferenceLibraryBean(library).books,
        equalTo(
            ImmutableSet.of(
                new ReferenceBookBean(ReferenceBook.builder("book1").build()),
                new ReferenceBookBean(ReferenceBook.builder("book2").build()))));
  }
}
