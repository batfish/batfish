package org.batfish.referencelibrary;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Class that describes the reference library information */
@ParametersAreNonnullByDefault
public class ReferenceLibrary {
  private static final String PROP_BOOKS = "books";

  private @Nonnull SortedSet<ReferenceBook> _books;

  /**
   * The argument to this constructor is a List (not a SortedSet) to prevent Jackson from silently
   * removing duplicate entries.
   */
  @JsonCreator
  public ReferenceLibrary(@JsonProperty(PROP_BOOKS) @Nullable List<ReferenceBook> books) {
    List<ReferenceBook> nnBooks = firstNonNull(books, ImmutableList.of());
    checkDuplicates(
        "book", nnBooks.stream().map(ReferenceBook::getName).collect(Collectors.toList()));

    _books =
        nnBooks.stream()
            .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
  }

  /** Does the provided collection have duplicates? */
  static void checkDuplicates(String objectType, List<String> list) {
    Map<String, Integer> counts = new HashMap<>();
    list.forEach(e -> counts.put(e, counts.computeIfAbsent(e, v -> 0) + 1));
    List<String> duplicates =
        counts.entrySet().stream()
            .filter(e -> e.getValue() > 1)
            .map(e -> e.getKey())
            .collect(Collectors.toList());
    checkArgument(duplicates.isEmpty(), "Duplicate %s name(s): %s", objectType, duplicates);
  }

  /** Deletes the book */
  public void delAddressBook(String bookName) {
    ReferenceBook book =
        getReferenceBook(bookName)
            .orElseThrow(() -> new NoSuchElementException("Book not found: " + bookName));
    _books =
        _books.stream()
            .filter(b -> !b.equals(book))
            .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (!(o instanceof ReferenceLibrary)) {
      return false;
    }
    return Objects.equals(_books, ((ReferenceLibrary) o)._books);
  }

  /** Returns the specified dimension in this ReferenceLibrary object */
  public @Nonnull Optional<ReferenceBook> getReferenceBook(String bookName) {
    return _books.stream().filter(d -> d.getName().equals(bookName)).findFirst();
  }

  @JsonProperty(PROP_BOOKS)
  public @Nonnull SortedSet<ReferenceBook> getReferenceBooks() {
    return _books;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_books);
  }

  /**
   * Returns a new reference library additionally containing the reference books in {@code
   * newBooks}. Books with the same name are overwritten.
   */
  public @Nonnull ReferenceLibrary mergeReferenceBooks(Collection<ReferenceBook> newBooks) {
    List<ReferenceBook> booksToKeep =
        _books.stream()
            .filter(
                originalBook ->
                    !newBooks.stream()
                        .anyMatch(newBook -> newBook.getName().equals(originalBook.getName())))
            .collect(Collectors.toList());

    booksToKeep.addAll(newBooks);
    return new ReferenceLibrary(booksToKeep);
  }
}
