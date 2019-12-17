package org.batfish.coordinator.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.referencelibrary.ReferenceLibrary;

@ParametersAreNonnullByDefault
public class ReferenceLibraryBean {

  /** The set of {@link ReferenceBookBean}s in this library */
  public Set<ReferenceBookBean> books;

  @JsonCreator
  private ReferenceLibraryBean() {}

  public ReferenceLibraryBean(ReferenceLibrary library) {
    books =
        library.getReferenceBooks().stream()
            .map(book -> new ReferenceBookBean(book))
            .collect(Collectors.toSet());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ReferenceLibraryBean)) {
      return false;
    }
    return Objects.equals(books, ((ReferenceLibraryBean) o).books);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(books);
  }
}
