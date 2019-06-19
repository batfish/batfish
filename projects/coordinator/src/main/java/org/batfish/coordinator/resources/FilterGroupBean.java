package org.batfish.coordinator.resources;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Objects;
import org.batfish.referencelibrary.FilterGroup;

public class FilterGroupBean {

  /**
   * The set of filters in this filter group. Each filter must be a valid input to currently active
   * {@link org.batfish.specifier.FilterSpecifier}.
   */
  public List<String> filters;

  /** The name of this address group */
  public String name;

  @JsonCreator
  private FilterGroupBean() {}

  public FilterGroupBean(FilterGroup filterGroup) {
    name = filterGroup.getName();
    filters = ImmutableList.copyOf(filterGroup.getFilters());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof FilterGroupBean)) {
      return false;
    }
    return Objects.equals(filters, ((FilterGroupBean) o).filters)
        && Objects.equals(name, ((FilterGroupBean) o).name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(filters, name);
  }

  public FilterGroup toFilterGroup() {
    return new FilterGroup(ImmutableList.copyOf(firstNonNull(filters, ImmutableSet.of())), name);
  }
}
