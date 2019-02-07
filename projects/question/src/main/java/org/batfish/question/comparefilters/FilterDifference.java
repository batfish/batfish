package org.batfish.question.comparefilters;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A difference between two versions of a filter. */
public final class FilterDifference {
  private final @Nonnull String _hostname;
  private final @Nonnull String _filterName;
  private final @Nullable Integer _index;
  private final @Nullable Integer _referenceIndex;

  public FilterDifference(
      @Nonnull String hostname,
      @Nonnull String filterName,
      @Nullable Integer index,
      @Nullable Integer referenceIndex) {
    _hostname = hostname;
    _filterName = filterName;
    _index = index;
    _referenceIndex = referenceIndex;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FilterDifference)) {
      return false;
    }
    FilterDifference that = (FilterDifference) o;
    return Objects.equals(_hostname, that._hostname)
        && Objects.equals(_filterName, that._filterName)
        && Objects.equals(_index, that._index)
        && Objects.equals(_referenceIndex, that._referenceIndex);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _filterName, _index, _referenceIndex);
  }

  /** Return the name of the filter */
  @Nonnull
  public String getFilterName() {
    return _filterName;
  }

  /** Return the hostname of the filter */
  @Nonnull
  public String getHostname() {
    return _hostname;
  }

  /**
   * Return the index of the matched line in the current filter, or null if the difference is for
   * when no line matches.
   */
  @Nullable
  public Integer getCurrentIndex() {
    return _index;
  }

  /**
   * Return the index of the matched line in the reference filter, or null if the difference is for
   * when no line matches.
   */
  @Nullable
  public Integer getReferenceIndex() {
    return _referenceIndex;
  }
}
