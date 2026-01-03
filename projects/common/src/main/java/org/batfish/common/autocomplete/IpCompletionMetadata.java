package org.batfish.common.autocomplete;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.RangeSet;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** Metadata about an Ip needed for autocomplete. */
@ParametersAreNonnullByDefault
public final class IpCompletionMetadata implements Serializable {
  private final @Nullable RangeSet<Ip> _ipSubset;

  // TODO: Why does insertion order matter?
  private final @Nonnull LinkedHashSet<IpCompletionRelevance> _relevances;

  public IpCompletionMetadata() {
    this(null, ImmutableList.of());
  }

  public IpCompletionMetadata(IpCompletionRelevance relevance) {
    this(null, ImmutableList.of(relevance));
  }

  public IpCompletionMetadata(List<IpCompletionRelevance> relevances) {
    this(null, relevances);
  }

  public IpCompletionMetadata(
      @Nullable RangeSet<Ip> ipSubset, List<IpCompletionRelevance> relevances) {
    _ipSubset = ipSubset == null ? null : ImmutableRangeSet.copyOf(ipSubset);
    _relevances = new LinkedHashSet<>(relevances);
  }

  /** Add another relevance with the specified display and match tags. */
  public void addRelevance(IpCompletionRelevance relevance) {
    _relevances.add(relevance);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IpCompletionMetadata)) {
      return false;
    }
    IpCompletionMetadata that = (IpCompletionMetadata) o;
    return Objects.equals(_ipSubset, that._ipSubset) && _relevances.equals(that._relevances);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ipSubset, _relevances);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("_relevances", _relevances).toString();
  }

  /**
   * A {@link RangeSet} of IPs that have the same relevances. Taken to be the subset of the related
   * Ip or Prefix. If {@code null}, all IPs in the full set have the same relevances.
   */
  public @Nullable RangeSet<Ip> getIpSubset() {
    return _ipSubset;
  }

  /** List of reasons why this IP is relevant */
  public @Nonnull List<IpCompletionRelevance> getRelevances() {
    return ImmutableList.copyOf(_relevances);
  }
}
