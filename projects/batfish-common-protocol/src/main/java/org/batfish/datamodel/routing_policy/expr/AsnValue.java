package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.routing_policy.Environment;

/** Evaluates to the value of the given ASN. */
public final class AsnValue extends LongExpr {
  public static AsnValue of(@Nonnull AsExpr asExpr) {
    return new AsnValue(asExpr);
  }

  @Override
  public <T, U> T accept(LongExprVisitor<T, U> visitor, U arg) {
    return visitor.visitAsnValue(this, arg);
  }

  @Override
  public long evaluate(Environment environment) {
    return _asExpr.evaluate(environment);
  }

  @JsonProperty(PROP_AS)
  public @Nonnull AsExpr getAsExpr() {
    return _asExpr;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof AsnValue && ((AsnValue) obj)._asExpr.equals(_asExpr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(AsnValue.class, _asExpr);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("asExpr", _asExpr).toString();
  }

  private AsnValue(@Nonnull AsExpr asExpr) {
    _asExpr = asExpr;
  }

  @JsonCreator
  private static @Nonnull AsnValue jsonCreator(@JsonProperty(PROP_AS) @Nullable AsExpr as) {
    checkArgument(as != null, "Missing %s", PROP_AS);
    return new AsnValue(as);
  }

  private static final String PROP_AS = "as";
  private final @Nonnull AsExpr _asExpr;
}
