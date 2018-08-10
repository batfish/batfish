package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.Configuration;
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
    checkArgument(
        ctxt.getConfigs().containsKey(node),
        "SpecifierContext does not have configs for node " + node);
    Configuration config = ctxt.getConfigs().get(node);

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

    return config
        .getIpAccessLists()
        .values()
        .stream()
        .filter(
            filter ->
                filterGroup
                    .getFilters()
                    .stream()
                    .anyMatch(specifier -> specifier.matches(filter, config)))
        .collect(Collectors.toSet());
  }
}
