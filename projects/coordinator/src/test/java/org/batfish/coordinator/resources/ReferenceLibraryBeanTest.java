package org.batfish.coordinator.resources;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.junit.Test;

public class ReferenceLibraryBeanTest {

  /** Test if empty library is bean'd properly */
  @Test
  public void constructorEmpty() throws IOException {
    ReferenceLibrary library = new ReferenceLibrary(ImmutableList.of());

    assertThat(new ReferenceLibraryBean(library).books, equalTo(ImmutableSortedSet.of()));
  }

  /** Test if a non-empty library is bean'd properly */
  @Test
  public void constructorNonEmpty() throws IOException {
    ReferenceLibrary library =
        new ReferenceLibrary(
            ImmutableList.of(
                new ReferenceBook(null, null, "book1", null, null, null),
                new ReferenceBook(null, null, "book2", null, null, null)));

    assertThat(
        new ReferenceLibraryBean(library).books,
        equalTo(
            ImmutableSet.of(
                new ReferenceBookBean(new ReferenceBook(null, null, "book1", null, null, null)),
                new ReferenceBookBean(new ReferenceBook(null, null, "book2", null, null, null)))));
  }
}
