package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.util.CommonUtil;

@JsonSchemaDescription(
    "Represents a named access-list whose matching criteria is restricted to regexes on community "
        + "attributes sent with a bgp advertisement")
public class CommunityList implements Serializable {

  private static final String PROP_LINES = "lines";

  private static final String PROP_NAME = "name";

  private static final long serialVersionUID = 1L;

  private boolean _invertMatch;

  /**
   * The list of lines that are checked in order against the community attribute(s) of a bgp
   * advertisement
   */
  @Nonnull private final List<CommunityListLine> _lines;

  @Nonnull private final String _name;

  private transient LoadingCache<Long, Boolean> _communityCache;

  private transient LoadingCache<String, Pattern> _patternCache;

  @JsonCreator
  private static CommunityList newCommunityList(
      @Nullable @JsonProperty(PROP_NAME) String name,
      @Nullable @JsonProperty(PROP_LINES) List<CommunityListLine> lines) {
    return new CommunityList(requireNonNull(name), firstNonNull(lines, ImmutableList.of()));
  }

  /**
   * Constructs a CommunityList with the given name for {@link #_name}, and lines for {@link
   * #_lines}
   *
   * @param name The name of the structure
   * @param lines The lines in the list
   */
  public CommunityList(@Nonnull String name, @Nonnull List<CommunityListLine> lines) {
    _name = name;
    _lines = lines;
    initCaches();
  }

  /** Check if any line matches given community */
  private boolean computeIfMatches(long community) {
    Optional<LineAction> action =
        _lines
            .stream()
            .map(line -> executeLineMatch(community, line))
            .filter(Objects::nonNull)
            .findFirst();

    // "invert != condition" is a concise way of inverting a boolean
    return action.isPresent() && _invertMatch != (action.get() == LineAction.ACCEPT);
  }

  /** Check if line matches community. If yes, return line action, otherwise {@code null}. */
  @Nullable
  private LineAction executeLineMatch(long community, CommunityListLine line) {
    Pattern p = _patternCache.getUnchecked(line.getRegex());
    String communityStr = CommonUtil.longToCommunity(community);
    Matcher matcher = p.matcher(communityStr);
    return matcher.find() ? line.getAction() : null;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof CommunityList)) {
      return false;
    }
    CommunityList other = (CommunityList) o;
    return _invertMatch == other._invertMatch && Objects.equals(_lines, other._lines);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_invertMatch, _lines);
  }

  private void initCaches() {
    _communityCache =
        CacheBuilder.newBuilder()
            .build(
                new CacheLoader<Long, Boolean>() {
                  @Override
                  public Boolean load(@Nonnull Long community) {
                    return computeIfMatches(community);
                  }
                });
    _patternCache =
        CacheBuilder.newBuilder()
            .build(
                new CacheLoader<String, Pattern>() {
                  @Override
                  public Pattern load(@Nonnull String regex) {
                    return Pattern.compile(regex);
                  }
                });
  }

  @JsonPropertyDescription(
      "Specifies whether or not lines should match the complement of their criteria (does not "
          + "change whether a line permits or denies).")
  public boolean getInvertMatch() {
    return _invertMatch;
  }

  @JsonProperty(PROP_LINES)
  @JsonPropertyDescription(
      "The list of lines that are checked in order against the community attribute(s) of a bgp "
          + "advertisement")
  public List<CommunityListLine> getLines() {
    return _lines;
  }

  @JsonProperty(PROP_NAME)
  @JsonPropertyDescription("The name of this community list")
  @Nonnull
  public String getName() {
    return _name;
  }

  /** Check if a given community is permitted/accepted by this list. */
  public boolean permits(long community) {
    return _communityCache.getUnchecked(community);
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    initCaches();
  }

  public void setInvertMatch(boolean invertMatch) {
    _invertMatch = invertMatch;
  }
}
