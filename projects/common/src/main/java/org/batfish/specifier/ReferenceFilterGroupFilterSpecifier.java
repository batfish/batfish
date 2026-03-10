package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import org.batfish.datamodel.IpAccessList;
import org.batfish.referencelibrary.FilterGroup;

/**
 * A {@link FilterSpecifier} that looks up an {@link org.batfish.referencelibrary.FilterGroup} in a
 * {@link org.batfish.referencelibrary.ReferenceBook}.
 */
public final class ReferenceFilterGroupFilterSpecifier implements FilterSpecifier {
  private final String _bookName;
  private final String _filterGroupName;

  public ReferenceFilterGroupFilterSpecifier(String filterGroupName, String bookName) {
    _filterGroupName = filterGroupName;
    _bookName = bookName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ReferenceFilterGroupFilterSpecifier)) {
      return false;
    }
    ReferenceFilterGroupFilterSpecifier other = (ReferenceFilterGroupFilterSpecifier) o;
    return Objects.equals(_filterGroupName, other._filterGroupName)
        && Objects.equals(_bookName, other._bookName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_filterGroupName, _bookName);
  }

  @Override
  public Set<IpAccessList> resolve(String node, SpecifierContext ctxt) {
    FilterGroup filterGroup =
        ctxt.getReferenceBook(_bookName)
            .orElseThrow(
                () -> new NoSuchElementException("ReferenceBook '" + _bookName + "' not found"))
            .getFilterGroup(_filterGroupName)
            .orElseThrow(
                () ->
                    new NoSuchElementException(
                        "FilterGroup '"
                            + _filterGroupName
                            + "' not found in ReferenceBook '"
                            + _bookName
                            + "'"));

    return filterGroup.getFilters().stream()
        .flatMap(
            f ->
                SpecifierFactories.getFilterSpecifierOrDefault(f, NoFiltersFilterSpecifier.INSTANCE)
                    .resolve(node, ctxt)
                    .stream())
        .collect(ImmutableSet.toImmutableSet());
  }
}
