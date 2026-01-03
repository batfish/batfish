package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Evaluates to true when evaluated in an environment with {@link BgpSessionProperties} present and
 * of the specified types.
 */
@ParametersAreNonnullByDefault
public final class MatchBgpSessionType extends BooleanExpr {
  private static final String PROP_TYPES = "types";

  /** Which types of BgpSessionProperties to match. */
  public enum Type {
    /** Matches any EBGP type in {@link BgpSessionProperties.SessionType}. */
    EBGP,
    /** Matches any IBGP type in {@link BgpSessionProperties.SessionType}. */
    IBGP,
  }

  private final @Nonnull EnumSet<Type> _types;

  public MatchBgpSessionType(Type... types) {
    this(Arrays.asList(types));
  }

  public MatchBgpSessionType(Collection<Type> types) {
    checkArgument(!types.isEmpty(), "Must match at least 1 type");
    _types = EnumSet.copyOf(types);
  }

  @JsonCreator
  private static MatchBgpSessionType create(
      @JsonProperty(PROP_TYPES) @Nullable Collection<Type> types) {
    checkArgument(types != null, "Missing %s", PROP_TYPES);
    return new MatchBgpSessionType(types);
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchBgpSessionType(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    @Nullable BgpSessionProperties props = environment.getBgpSessionProperties();
    if (props == null) {
      return new Result(false);
    }
    return switch (props.getSessionType()) {
      case EBGP_SINGLEHOP, EBGP_MULTIHOP, EBGP_UNNUMBERED -> new Result(_types.contains(Type.EBGP));
      case IBGP, IBGP_UNNUMBERED -> new Result(_types.contains(Type.IBGP));
      case UNSET -> throw new IllegalStateException("Established session cannot have type UNSET");
    };
  }

  @JsonProperty(PROP_TYPES)
  public @Nonnull Set<Type> getTypes() {
    return _types;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MatchBgpSessionType)) {
      return false;
    }
    MatchBgpSessionType that = (MatchBgpSessionType) o;
    return _types.equals(that._types);
  }

  @Override
  public int hashCode() {
    // Consistent hash for a set of enums: the Set hashcode algorithm but on ordinal value.
    int hash = 0;
    for (Type t : _types) {
      hash += t.ordinal();
    }
    return hash;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(MatchBgpSessionType.class).add(PROP_TYPES, _types).toString();
  }
}
