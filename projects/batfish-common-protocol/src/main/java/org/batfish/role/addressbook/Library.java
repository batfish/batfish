package org.batfish.role.addressbook;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;

/** Class that describes the address library information */
public class Library {

  /**
   * Describes valid names for library objects. Must start with letters or underscore, and only
   * contain {-,\w} ( i.e., [-a-zA-Z_0-9])
   */
  public static final String NAME_PATTERN = "[a-zA-Z_][-\\w]*";

  private static final Pattern _NAME_PATTERN = Pattern.compile(NAME_PATTERN);

  @Nonnull private SortedSet<Book> _books;

  /**
   * The argument to this constructor is a List (not a SortedSet) to prevent Jackson from silently
   * removing duplicate entries.
   */
  @JsonCreator
  public Library(List<Book> books) {
    checkArgument(books != null, "Library cannot have a null list of books (empty list is OK)");
    checkDuplicates("book", books.stream().map(Book::getName).collect(Collectors.toList()));

    _books =
        books.stream().collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
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
        Library.NAME_PATTERN);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Library)) {
      return false;
    }
    return Objects.equals(_books, ((Library) o)._books);
  }

  /** Returns the specified dimension in this AddressLibrary object */
  public Optional<Book> getBook(String bookName) {
    return _books.stream().filter(d -> d.getName().equals(bookName)).findFirst();
  }

  /**
   * From the {@link Library} at {@code dataPath}, get the {@link Book} with name {@code bookName}.
   *
   * @throws IOException If the contents of the file could not be cast to {@link Library}
   */
  public static Optional<Book> getBook(@Nonnull Path dataPath, @Nonnull String bookName)
      throws IOException {
    Library data = read(dataPath);
    return data.getBook(bookName);
  }

  @JsonValue
  public SortedSet<Book> getBooks() {
    return _books;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_books);
  }

  /**
   * Reads the {@link Library} object from {@code dataPath}. If the path does not exist, initializes
   * a new object.
   *
   * @throws IOException If file exists but its contents could not be cast to {@link Library}
   */
  public static Library read(Path dataPath) throws IOException {
    if (Files.exists(dataPath)) {
      return BatfishObjectMapper.mapper().readValue(CommonUtil.readFile(dataPath), Library.class);
    }
    return new Library(null);
  }

  public static synchronized void write(Library data, Path dataPath)
      throws JsonProcessingException {
    CommonUtil.writeFile(dataPath, BatfishObjectMapper.writePrettyString(data));
  }
}
