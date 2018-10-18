package org.batfish.datamodel.flow;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;

/** Represents one of the paths found for a {@link Flow} */
public final class Trace {

  private static final String PROP_DISPOSITION = "disposition";
  private static final String PROP_HOPS = "hops";

  /** Final action taken on the last {@link Step} of the last {@link Hop} of the {@link Trace} */
  @Nonnull private final FlowDisposition _disposition;

  /** List of {@link Hop}s making up {@link Trace} */
  @Nonnull private final List<Hop> _hops;

  public Trace(FlowDisposition disposition, List<Hop> hops) {
    _disposition = disposition;
    _hops = hops;
  }

  @JsonCreator
  private static Trace jsonCreator(
      @JsonProperty(PROP_DISPOSITION) @Nullable FlowDisposition disposition,
      @JsonProperty(PROP_HOPS) @Nullable List<Hop> hops) {
    checkArgument(disposition != null, "Missing %s", PROP_DISPOSITION);
    return new Trace(disposition, firstNonNull(hops, ImmutableList.of()));
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof Trace)) {
      return false;
    }
    Trace rhs = (Trace) o;
    return _disposition == rhs._disposition && _hops.equals(rhs._hops);
  }

  @JsonProperty(PROP_DISPOSITION)
  @Nonnull
  public FlowDisposition getDisposition() {
    return _disposition;
  }

  @JsonProperty(PROP_HOPS)
  @Nonnull
  public List<Hop> getHops() {
    return _hops;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hops, _disposition.ordinal());
  }
}
