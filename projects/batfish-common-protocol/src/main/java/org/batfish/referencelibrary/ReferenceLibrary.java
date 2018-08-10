package org.batfish.referencelibrary;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;

/** Class that describes the reference library information */
public class ReferenceLibrary {

  /**
   * Describes valid names for library objects. Must start with letters or underscore, and only
   * contain {-,\w} ( i.e., [-a-zA-Z_0-9])
   */
  public static final String NAME_PATTERN = "[a-zA-Z_][-\\w]*";

  private static final Pattern _NAME_PATTERN = Pattern.compile(NAME_PATTERN);

  private static final String PROP_BOOKS = "books";

  @Nonnull private SortedSet<ReferenceBook> _books;

  /**
   * The argument to this constructor is a List (not a SortedSet) to prevent Jackson from silently
   * removing duplicate entries.
   */
  @JsonCreator
  public ReferenceLibrary(@JsonProperty(PROP_BOOKS) List<ReferenceBook> books) {
    List<ReferenceBook> nnBooks = firstNonNull(books, ImmutableList.of());
    checkDuplicates(
        "book", nnBooks.stream().map(ReferenceBook::getName).collect(Collectors.toList()));

    _books =
        nnBooks
            .stream()
            .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
  }

  /** Does the provided collection have duplicates? */
  static void checkDuplicates(String objectType, List<String> list) {
    Map<String, Integer> counts = new HashMap<>();
    list.forEach(e -> counts.put(e, counts.computeIfAbsent(e, v -> 0) + 1));
    List<String> duplicates =
        counts
            .entrySet()
            .stream()
            .filter(e -> e.getValue() > 1)
            .map(e -> e.getKey())
            .collect(Collectors.toList());
    checkArgument(duplicates.isEmpty(), "Duplicate %s name(s): %s", objectType, duplicates);
  }

  /** Is this is valid library object name? */
  static void checkValidName(String name, String objectType) {
    checkArgument(
        _NAME_PATTERN.matcher(name).matches(),
        "Invalid '%s' name '%s'. Valid names match '%s'",
        objectType,
        name,
        ReferenceLibrary.NAME_PATTERN);
  }

  /** Deletes the book */
  public void delAddressBook(String bookName) {
    ReferenceBook book =
        getReferenceBook(bookName)
            .orElseThrow(() -> new NoSuchElementException("Book not found: " + bookName));
    _books =
        _books
            .stream()
            .filter(b -> !b.equals(book))
            .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ReferenceLibrary)) {
      return false;
    }
    return Objects.equals(_books, ((ReferenceLibrary) o)._books);
  }

  /** Returns the specified dimension in this ReferenceLibrary object */
  public Optional<ReferenceBook> getReferenceBook(String bookName) {
    return _books.stream().filter(d -> d.getName().equals(bookName)).findFirst();
  }

  /**
   * From the {@link ReferenceLibrary} at {@code dataPath}, get the {@link ReferenceBook} with name
   * {@code bookName}.
   *
   * @throws IOException If the contents of the file could not be cast to {@link ReferenceLibrary}
   */
  public static Optional<ReferenceBook> getReferenceBook(
      @Nonnull Path dataPath, @Nonnull String bookName) throws IOException {
    ReferenceLibrary data = read(dataPath);
    return data.getReferenceBook(bookName);
  }

  @JsonProperty(PROP_BOOKS)
  public SortedSet<ReferenceBook> getReferenceBooks() {
    return _books;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_books);
  }

  /**
   * Adds the reference books in {@code newBooks} to the ReferenceLibrary at {@code path}. Books
   * with the same name are overwritten
   */
  public static void mergeReferenceBooks(
      @Nonnull Path path, @Nonnull SortedSet<ReferenceBook> newBooks) throws IOException {

    ReferenceLibrary originalLibrary = read(path);

    List<ReferenceBook> booksToKeep =
        originalLibrary
            ._books
            .stream()
            .filter(
                originalBook ->
                    !newBooks
                        .stream()
                        .anyMatch(newBook -> newBook.getName().equals(originalBook.getName())))
            .collect(Collectors.toList());

    booksToKeep.addAll(newBooks);
    ReferenceLibrary newLibrary = new ReferenceLibrary(booksToKeep);

    write(newLibrary, path);
  }

  /**
   * Reads the {@link ReferenceLibrary} object from {@code dataPath}. If the path does not exist,
   * initializes a new object.
   *
   * @throws IOException If file exists but its contents could not be cast to {@link
   *     ReferenceLibrary}
   */
  public static ReferenceLibrary read(Path dataPath) throws IOException {
    if (Files.exists(dataPath)) {
      return BatfishObjectMapper.mapper()
          .readValue(CommonUtil.readFile(dataPath), ReferenceLibrary.class);
    }
    return new ReferenceLibrary(null);
  }

  public static synchronized void write(ReferenceLibrary data, Path dataPath)
      throws JsonProcessingException {
    CommonUtil.writeFile(dataPath, BatfishObjectMapper.writePrettyString(data));
  }
}
