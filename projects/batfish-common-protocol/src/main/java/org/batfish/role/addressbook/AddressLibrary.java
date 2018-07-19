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
public class AddressLibrary {

  /**
   * Describes valid names for library objects. Must start with letters or underscore, and only
   * contain {-,\w} ( i.e., [-a-zA-Z_0-9])
   */
  public static final String NAME_PATTERN = "[a-zA-Z_][-\\w]*";

  private static final Pattern _NAME_PATTERN = Pattern.compile(NAME_PATTERN);

  @Nonnull private SortedSet<AddressBook> _books;

  /**
   * The argument to this constructor is a List (not a SortedSet) to prevent Jackson from silently
   * removing duplicate entries.
   */
  @JsonCreator
  public AddressLibrary(List<AddressBook> books) {
    checkArgument(books != null, "Library cannot have a null list of books (empty list is OK)");
    checkDuplicates("book", books.stream().map(AddressBook::getName).collect(Collectors.toList()));

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
        AddressLibrary.NAME_PATTERN);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AddressLibrary)) {
      return false;
    }
    return Objects.equals(_books, ((AddressLibrary) o)._books);
  }

  /** Returns the specified dimension in this AddressLibrary object */
  public Optional<AddressBook> getAddressBook(String bookName) {
    return _books.stream().filter(d -> d.getName().equals(bookName)).findFirst();
  }

  /**
   * From the {@link AddressLibrary} at {@code dataPath}, get the {@link AddressBook} with name
   * {@code bookName}.
   *
   * @throws IOException If the contents of the file could not be cast to {@link AddressLibrary}
   */
  public static Optional<AddressBook> getAddressBook(
      @Nonnull Path dataPath, @Nonnull String bookName) throws IOException {
    AddressLibrary data = read(dataPath);
    return data.getAddressBook(bookName);
  }

  @JsonValue
  public SortedSet<AddressBook> getAddressBooks() {
    return _books;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_books);
  }

  /**
   * Adds the address books in {@code newBooks} to the AddressLibrary at {@code path}. Books with
   * the same name are overwritten
   */
  public static void mergeAddressBooks(@Nonnull Path path, @Nonnull SortedSet<AddressBook> newBooks)
      throws IOException {

    AddressLibrary originalLibrary = read(path);

    List<AddressBook> booksToKeep =
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
    AddressLibrary newLibrary = new AddressLibrary(booksToKeep);

    write(newLibrary, path);
  }

  /**
   * Reads the {@link AddressLibrary} object from {@code dataPath}. If the path does not exist,
   * initializes a new object.
   *
   * @throws IOException If file exists but its contents could not be cast to {@link AddressLibrary}
   */
  public static AddressLibrary read(Path dataPath) throws IOException {
    if (Files.exists(dataPath)) {
      return BatfishObjectMapper.mapper()
          .readValue(CommonUtil.readFile(dataPath), AddressLibrary.class);
    }
    return new AddressLibrary(null);
  }

  public static synchronized void write(AddressLibrary data, Path dataPath)
      throws JsonProcessingException {
    CommonUtil.writeFile(dataPath, BatfishObjectMapper.writePrettyString(data));
  }
}
