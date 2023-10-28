package org.batfish.datamodel.packet_policy;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.acl.AclLineMatchExpr;

/**
 * A boolean expression that returns true if a packet/flow matches a given {@link AclLineMatchExpr}
 */
public final class PacketMatchExpr implements BoolExpr {

  private static final String PROP_EXPR = "expression";

  private final AclLineMatchExpr _expr;

  public PacketMatchExpr(AclLineMatchExpr expr) {
    _expr = expr;
  }

  @JsonCreator
  private static PacketMatchExpr jsonCreator(
      @JsonProperty(PROP_EXPR) @Nullable AclLineMatchExpr expr) {
    checkArgument(expr != null, "Missing %s", PROP_EXPR);
    return new PacketMatchExpr(expr);
  }

  @Override
  public <T> T accept(BoolExprVisitor<T> visitor) {
    return visitor.visitPacketMatchExpr(this);
  }

  @JsonProperty(PROP_EXPR)
  public AclLineMatchExpr getExpr() {
    return _expr;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PacketMatchExpr that = (PacketMatchExpr) o;
    return Objects.equals(getExpr(), that.getExpr());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getExpr());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("expr", _expr).toString();
  }
}
