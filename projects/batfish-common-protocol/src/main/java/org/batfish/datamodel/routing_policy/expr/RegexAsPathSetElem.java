package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class RegexAsPathSetElem extends AsPathSetElem {
  private static final String PROP_REGEX = "regex";

  @Nonnull private String _regex;

  @JsonCreator
  private static RegexAsPathSetElem jsonCreator(@Nullable @JsonProperty(PROP_REGEX) String regex) {
    checkArgument(regex != null, "%s must be provided", PROP_REGEX);
    return new RegexAsPathSetElem(regex);
  }

  public RegexAsPathSetElem(String regex) {
    _regex = regex;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof RegexAsPathSetElem)) {
      return false;
    }
    RegexAsPathSetElem other = (RegexAsPathSetElem) obj;
    return _regex.equals(other._regex);
  }

  @JsonProperty(PROP_REGEX)
  @Nonnull
  public String getRegex() {
    return _regex;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _regex.hashCode();
    return result;
  }

  @Override
  public String regex() {
    return getRegex();
  }

  public void setRegex(String regex) {
    _regex = regex;
  }
}
