package org.batfish.common.autocomplete;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Metadata about an Ip needed for autocomplete. */
@ParametersAreNonnullByDefault
public final class IpCompletionMetadata implements Serializable {

  private static final String PROP_RELEVANCES = "relevances";

  public enum Reason {
    // more reasons to come later
    INTERFACE_IP
  }

  /** Represents one reason why this IP is relevant */
  public static final class Relevance {

    private static final String PROP_REASON = "reason";
    private static final String PROP_MATCH_OBJECT = "matchObject";

    @Nonnull private final Reason _reason;

    @Nonnull private final String _matchObject;

    @JsonCreator
    private static Relevance jsonCreator(
        @Nullable @JsonProperty(PROP_REASON) Reason reason,
        @Nullable @JsonProperty(PROP_MATCH_OBJECT) String matchObject) {
      checkNotNull(reason, "Reason for Relevance cannot be null");
      checkNotNull(matchObject, "MatchObject for Relevance cannot be null");
      return new Relevance(reason, matchObject);
    }

    public Relevance(Reason reason, String matchObject) {
      _reason = reason;
      _matchObject = matchObject;
    }

    /** Type of relevance */
    @JsonProperty(PROP_REASON)
    public Reason getReason() {
      return _reason;
    }

    /** String representation to match on for the underlying object that leads to the relevance. */
    @JsonProperty(PROP_MATCH_OBJECT)
    public String getMatchObject() {
      return _matchObject;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Relevance)) {
        return false;
      }
      Relevance relevance = (Relevance) o;
      return _reason == relevance._reason && _matchObject.equals(relevance._matchObject);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_reason, _matchObject);
    }
  }

  @Nonnull private final List<Relevance> _relevances;

  public IpCompletionMetadata() {
    this(ImmutableList.of());
  }

  public IpCompletionMetadata(List<Relevance> relevances) {
    _relevances = ImmutableList.copyOf(relevances);
  }

  @JsonCreator
  private static IpCompletionMetadata jsonCreator(
      @Nullable @JsonProperty(PROP_RELEVANCES) List<Relevance> relevances) {
    return new IpCompletionMetadata(firstNonNull(relevances, ImmutableList.of()));
  }

  /** Add another relevance */
  public void addRelevance(Relevance relevance) {
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

  /** List of reasons why this IP is relevant */
  @JsonProperty(PROP_RELEVANCES)
  @Nonnull
  public List<Relevance> getRelevances() {
    return _relevances;
  }
}
