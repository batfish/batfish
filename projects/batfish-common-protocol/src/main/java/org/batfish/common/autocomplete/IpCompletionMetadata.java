package org.batfish.common.autocomplete;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Metadata about an Ip needed for autocomplete. */
@ParametersAreNonnullByDefault
public final class IpCompletionMetadata implements Serializable {

  private static final String PROP_RELEVANCES = "relevances";

  @Nonnull private final List<IpCompletionRelevance> _relevances;

  public IpCompletionMetadata() {
    this(ImmutableList.of());
  }

  public IpCompletionMetadata(IpCompletionRelevance relevance) {
    this(ImmutableList.of(relevance));
  }

  public IpCompletionMetadata(List<IpCompletionRelevance> relevances) {
    _relevances = new LinkedList<>(relevances);
  }

  @JsonCreator
  private static IpCompletionMetadata jsonCreator(
      @Nullable @JsonProperty(PROP_RELEVANCES) List<IpCompletionRelevance> relevances) {
    return new IpCompletionMetadata(firstNonNull(relevances, ImmutableList.of()));
  }

  /** Add another relevance with the specified display and match tags. */
  public void addRelevance(IpCompletionRelevance relevance) {
    if (!_relevances.contains(relevance)) {
      _relevances.add(relevance);
    }
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
    return _relevances.equals(that._relevances);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_relevances);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("_relevances", _relevances).toString();
  }

  /** List of reasons why this IP is relevant */
  @JsonProperty(PROP_RELEVANCES)
  @Nonnull
  public List<IpCompletionRelevance> getRelevances() {
    return ImmutableList.copyOf(_relevances);
  }
}
