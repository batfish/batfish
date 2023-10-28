package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A line in an AsPathAccessList */
@ParametersAreNonnullByDefault
public final class AsPathAccessListLine implements Serializable, Comparable<AsPathAccessListLine> {
  private static final String PROP_ACTION = "action";
  private static final String PROP_REGEX = "regex";

  private @Nonnull LineAction _action;

  private @Nonnull String _regex;

  @JsonCreator
  private static AsPathAccessListLine jsonCreator(
      @JsonProperty(PROP_ACTION) @Nullable LineAction action,
      @JsonProperty(PROP_REGEX) @Nullable String regex) {
    checkArgument(action != null, "%s must be provided", PROP_ACTION);
    checkArgument(regex != null, "%s must be provided", PROP_REGEX);
    return new AsPathAccessListLine(action, regex);
  }

  public AsPathAccessListLine(LineAction action, String regex) {
    _action = action;
    _regex = regex;
  }

  @Override
  public int compareTo(AsPathAccessListLine rhs) {
    int ret = _regex.compareTo(rhs._regex);
    return ret;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof AsPathAccessListLine)) {
      return false;
    }
    AsPathAccessListLine other = (AsPathAccessListLine) o;
    return _action == other._action && _regex.equals(other._regex);
  }

  /** The action the underlying access-list will take when this line matches a route. */
  @JsonProperty(PROP_ACTION)
  public @Nonnull LineAction getAction() {
    return _action;
  }

  /** The regex against which a route's AS-path will be compared. */
  @JsonProperty(PROP_REGEX)
  public @Nonnull String getRegex() {
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

  public void setAction(LineAction action) {
    _action = action;
  }

  public void setRegex(String regex) {
    _regex = regex;
  }
}
