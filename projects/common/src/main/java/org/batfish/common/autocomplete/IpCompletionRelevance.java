package org.batfish.common.autocomplete;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** Represents one reason why this IP is relevant. */
@ParametersAreNonnullByDefault
public class IpCompletionRelevance implements Serializable {

  private static final String PROP_DISPLAY = "display";
  private static final String PROP_MATCH_TAGS = "tags";

  private final @Nonnull String _display;

  private final @Nonnull List<String> _matchTags;

  @JsonCreator
  private static IpCompletionRelevance jsonCreator(
      @JsonProperty(PROP_DISPLAY) @Nullable String display,
      @JsonProperty(PROP_MATCH_TAGS) @Nullable List<String> matchTags) {
    checkNotNull(display, "Display for Relevance cannot be null");
    return new IpCompletionRelevance(display, firstNonNull(matchTags, ImmutableList.of()));
  }

  public IpCompletionRelevance(String display, String... matchTags) {
    this(display, Arrays.stream(matchTags).collect(Collectors.toList()));
  }

  /**
   * Creates a new {@link IpCompletionRelevance} object. Removes any null or empty tags from
   * matchTags
   */
  public IpCompletionRelevance(String display, List<String> matchTags) {
    _display = display;
    _matchTags =
        matchTags.stream()
            .filter(Objects::nonNull)
            .filter(tag -> !tag.isEmpty())
            .map(String::toLowerCase)
            .collect(ImmutableList.toImmutableList());
  }

  /**
   * Returns true if each subQuery is a substring of at least one tag or of the ip. Subqueries
   * should already be lower case.
   */
  public boolean matches(String[] subQueries, Ip ip) {
    return Arrays.stream(subQueries)
        .allMatch(
            sq ->
                ip.toString().contains(sq)
                    || _matchTags.stream().anyMatch(tag -> tag.contains(sq)));
  }

  /** The string to display when this relevance matches */
  @JsonProperty(PROP_DISPLAY)
  public String getDisplay() {
    return _display;
  }

  /** The tags that should match the query */
  @JsonProperty(PROP_MATCH_TAGS)
  public List<String> getMatchTags() {
    return _matchTags;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IpCompletionRelevance)) {
      return false;
    }
    IpCompletionRelevance relevance = (IpCompletionRelevance) o;
    return _display.equals(relevance._display) && _matchTags.equals(relevance._matchTags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_display, _matchTags);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("display", _display)
        .add("matchTags", _matchTags)
        .toString();
  }
}
