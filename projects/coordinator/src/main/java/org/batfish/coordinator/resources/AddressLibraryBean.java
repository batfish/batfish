package org.batfish.coordinator.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.role.addressbook.AddressLibrary;

@ParametersAreNonnullByDefault
public class AddressLibraryBean {

  /** The set of {@link AddressBookBean}s in this library */
  public Set<AddressBookBean> books;

  @JsonCreator
  private AddressLibraryBean() {}

  public AddressLibraryBean(AddressLibrary library) {
    books =
        library
            .getAddressBooks()
            .stream()
            .map(book -> new AddressBookBean(book))
            .collect(Collectors.toSet());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AddressLibraryBean)) {
      return false;
    }
    return Objects.equals(books, ((AddressLibraryBean) o).books);
  }

  @Override
  public int hashCode() {
    return Objects.hash(books);
  }
}
