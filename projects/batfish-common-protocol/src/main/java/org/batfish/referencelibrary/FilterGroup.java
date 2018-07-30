package org.batfish.referencelibrary;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.questions.FiltersSpecifier;

public class FilterGroup implements Comparable<FilterGroup> {

  private static final String PROP_FILTERS = "filters";
  private static final String PROP_NAME = "name";

  @Nonnull private List<FiltersSpecifier> _filters;
  @Nonnull private String _name;

  public FilterGroup(
      @Nullable @JsonProperty(PROP_FILTERS) List<FiltersSpecifier> filters,
      @Nullable @JsonProperty(PROP_NAME) String name) {
    checkArgument(name != null, "Filter group name cannot not be null");
    ReferenceLibrary.checkValidName(name, "filter group");

    _name = name;
    _filters = firstNonNull(filters, new LinkedList<>());
  }

  @Override
  public int compareTo(FilterGroup o) {
    return _name.compareTo(o._name);
  }

  @JsonProperty(PROP_FILTERS)
  @Nonnull
  public List<FiltersSpecifier> getFilters() {
    return _filters;
  }

  @JsonProperty(PROP_NAME)
  @Nonnull
  public String getName() {
    return _name;
  }
}
