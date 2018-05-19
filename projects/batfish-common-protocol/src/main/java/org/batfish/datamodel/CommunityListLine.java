package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.batfish.common.util.CommonUtil;

@JsonSchemaDescription("A line in a CommunityList")
public class CommunityListLine implements Serializable {

  private static final String PROP_ACTION = "action";

  private static final String PROP_REGEX = "regex";

  private static final long serialVersionUID = 1L;

  private final LineAction _action;

  private final String _regex;

  @JsonCreator
  public CommunityListLine(
      @JsonProperty(PROP_ACTION) LineAction action, @JsonProperty(PROP_REGEX) String regex) {
    _action = action;
    _regex = regex;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof CommunityListLine)) {
      return false;
    }
    CommunityListLine other = (CommunityListLine) o;
    if (_action != other._action) {
      return false;
    }
    if (!_regex.equals(other._regex)) {
      return false;
    }
    return true;
  }

  @JsonProperty(PROP_ACTION)
  @JsonPropertyDescription(
      "The action the underlying access-list will take when this line matches a route.")
  public LineAction getAction() {
    return _action;
  }

  public Set<Long> getExactMatchingCommunities(Set<Long> allCommunities) {
    Pattern p = Pattern.compile(_regex);
    Set<Long> matchingCommunitites = new LinkedHashSet<>();
    for (long candidateCommunity : allCommunities) {
      String candidateCommunityStr = CommonUtil.longToCommunity(candidateCommunity);
      Matcher matcher = p.matcher(candidateCommunityStr);
      if (matcher.matches()) {
        matchingCommunitites.add(candidateCommunity);
      }
    }
    return matchingCommunitites;
  }

  public Set<Long> getMatchingCommunities(Set<Long> allCommunities, boolean invertMatch) {
    Pattern p = Pattern.compile(_regex);
    Set<Long> matchingCommunitites = new LinkedHashSet<>();
    for (long candidateCommunity : allCommunities) {
      String candidateCommunityStr = CommonUtil.longToCommunity(candidateCommunity);
      Matcher matcher = p.matcher(candidateCommunityStr);
      if (matcher.find() ^ invertMatch) {
        matchingCommunitites.add(candidateCommunity);
      }
    }
    return matchingCommunitites;
  }

  @JsonProperty(PROP_REGEX)
  @JsonPropertyDescription("The regex against which a route's communities will be compared")
  public String getRegex() {
    return _regex;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _action.ordinal();
    result = prime * result + _regex.hashCode();
    return result;
  }

  public static Long toLiteralCommunity() {
    throw new UnsupportedOperationException("no implementation for generated method");
    // TODO Auto-generated method stub
  }
}
