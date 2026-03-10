package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RoleEdge implements Comparable<RoleEdge> {
  private static final String PROP_ROLE1 = "role1";
  private static final String PROP_ROLE2 = "role2";

  public RoleEdge(String role1, String role2) {
    _role1 = role1;
    _role2 = role2;
  }

  @JsonProperty(PROP_ROLE1)
  public @Nonnull String getRole1() {
    return _role1;
  }

  @JsonProperty(PROP_ROLE2)
  public @Nonnull String getRole2() {
    return _role2;
  }

  private final @Nonnull String _role1;
  private final @Nonnull String _role2;

  @JsonCreator
  private static RoleEdge jsonCreator(
      @JsonProperty(PROP_ROLE1) @Nullable String role1,
      @JsonProperty(PROP_ROLE2) @Nullable String role2) {
    checkArgument(role1 != null, "Missing %s", PROP_ROLE1);
    checkArgument(role2 != null, "Missing %s", PROP_ROLE2);
    return new RoleEdge(role1, role2);
  }

  @Override
  public int compareTo(RoleEdge o) {
    return Comparator.comparing(RoleEdge::getRole1)
        .thenComparing(RoleEdge::getRole2)
        .compare(this, o);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof RoleEdge)) {
      return false;
    }
    RoleEdge r = (RoleEdge) o;
    return _role1.equals(r._role1) && _role2.equals(r._role2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_role1, _role2);
  }

  @Override
  public String toString() {
    return "<" + _role1 + " --> " + _role2 + ">";
  }
}
