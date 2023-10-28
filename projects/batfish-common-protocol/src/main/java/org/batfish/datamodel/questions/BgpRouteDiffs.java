package org.batfish.datamodel.questions;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A list of {@link BgpRouteDiff}. */
@ParametersAreNonnullByDefault
public final class BgpRouteDiffs {
  private static final String PROP_DIFFS = "diffs";

  private final SortedSet<BgpRouteDiff> _diffs;

  public BgpRouteDiffs(Set<BgpRouteDiff> diffs) {
    _diffs = ImmutableSortedSet.copyOf(diffs);
  }

  @JsonCreator
  private static BgpRouteDiffs jsonCreator(
      @JsonProperty(PROP_DIFFS) @Nullable SortedSet<BgpRouteDiff> diffs) {
    return new BgpRouteDiffs(firstNonNull(diffs, ImmutableSortedSet.of()));
  }

  @JsonProperty(PROP_DIFFS)
  public SortedSet<BgpRouteDiff> getDiffs() {
    return _diffs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BgpRouteDiffs)) {
      return false;
    }
    BgpRouteDiffs that = (BgpRouteDiffs) o;
    return Objects.equals(_diffs, that._diffs);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_diffs);
  }
}
