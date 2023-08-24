package org.batfish.datamodel.tracking;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A method that succeeds when none of a list of conjunct {@link TrackMethod}s fails. */
public final class TrackAll implements TrackMethod {

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof TrackAll)) {
      return false;
    }
    return _conjuncts.equals(((TrackAll) obj)._conjuncts);
  }

  @Override
  public int hashCode() {
    return _conjuncts.hashCode();
  }

  @Override
  public <R> R accept(GenericTrackMethodVisitor<R> visitor) {
    return visitor.visitTrackAll(this);
  }

  @JsonProperty(PROP_CONJUNCTS)
  public @Nonnull List<TrackMethod> getConjuncts() {
    return _conjuncts;
  }

  static @Nonnull TrackAll of(List<TrackMethod> conjuncts) {
    return new TrackAll(ImmutableList.copyOf(conjuncts));
  }

  private static final String PROP_CONJUNCTS = "conjuncts";

  @JsonCreator
  private static @Nonnull TrackAll create(
      @JsonProperty(PROP_CONJUNCTS) @Nullable List<TrackMethod> conjuncts) {
    return of(firstNonNull(conjuncts, ImmutableList.of()));
  }

  private TrackAll(List<TrackMethod> conjuncts) {
    _conjuncts = conjuncts;
  }

  private final @Nonnull List<TrackMethod> _conjuncts;
}
