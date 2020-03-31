package org.batfish.datamodel.questions;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Contains a set of {@link AttributeDiff} objects */
@ParametersAreNonnullByDefault
public final class AttributeChanges {

  public static final AttributeChanges EMPTY = new AttributeChanges(ImmutableSortedSet.of());

  private static final String PROP_CHANGES = "Changes";

  @Nonnull private final SortedSet<AttributeDiff> _changes;

  @JsonCreator
  private static AttributeChanges jsonCreator(
      @JsonProperty(PROP_CHANGES) SortedSet<AttributeDiff> changes) {
    return new AttributeChanges(firstNonNull(changes, ImmutableSortedSet.of()));
  }

  public AttributeChanges(SortedSet<AttributeDiff> changes) {
    _changes = changes;
  }

  @Nonnull
  @JsonProperty(PROP_CHANGES)
  public SortedSet<AttributeDiff> getChanges() {
    return _changes;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AttributeChanges)) {
      return false;
    }
    AttributeChanges that = (AttributeChanges) o;
    return _changes.equals(that._changes);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_changes);
  }
}
