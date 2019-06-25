package org.batfish.referencelibrary;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Names;
import org.batfish.datamodel.Names.Type;

public class FilterGroup implements Comparable<FilterGroup>, Serializable {

  private static final String PROP_FILTERS = "filters";
  private static final String PROP_NAME = "name";

  @Nonnull private final List<String> _filters;
  @Nonnull private final String _name;

  public FilterGroup(
      @Nullable @JsonProperty(PROP_FILTERS) List<String> filters,
      @Nullable @JsonProperty(PROP_NAME) String name) {
    checkArgument(name != null, "Filter group name cannot not be null");
    Names.checkName(name, "filter group", Type.REFERENCE_OBJECT);

    _name = name;
    _filters =
        firstNonNull(filters, ImmutableList.<String>of()).stream()
            .filter(Objects::nonNull) // remove null values
            .collect(ImmutableList.toImmutableList());
  }

  @Override
  public int compareTo(FilterGroup o) {
    return _name.compareTo(o._name);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof FilterGroup)) {
      return false;
    }
    FilterGroup rhs = (FilterGroup) o;
    return Objects.equals(_name, rhs._name) && Objects.equals(_filters, ((FilterGroup) o)._filters);
  }

  @JsonProperty(PROP_FILTERS)
  @Nonnull
  public List<String> getFilters() {
    return _filters;
  }

  @JsonProperty(PROP_NAME)
  @Nonnull
  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _filters);
  }
}
