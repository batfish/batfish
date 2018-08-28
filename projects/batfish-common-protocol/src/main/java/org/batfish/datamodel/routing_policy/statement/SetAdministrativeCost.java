package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.IntExpr;

/**
 * Type of {@link Statement} to set the administrative cost of output route present in the {@link
 * Environment}
 */
@ParametersAreNonnullByDefault
public final class SetAdministrativeCost extends Statement {
  private static final String PROP_ADMIN = "admin";

  /** */
  private static final long serialVersionUID = 1L;

  @Nonnull private final IntExpr _admin;

  @JsonCreator
  private static SetAdministrativeCost jsonCreator(
      @Nullable @JsonProperty(PROP_ADMIN) IntExpr admin) {
    checkArgument(admin != null, "%s must be provided", PROP_ADMIN);
    return new SetAdministrativeCost(admin);
  }

  public SetAdministrativeCost(IntExpr admin) {
    _admin = admin;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof SetAdministrativeCost)) {
      return false;
    }
    SetAdministrativeCost other = (SetAdministrativeCost) obj;
    return _admin.equals(other._admin);
  }

  @Override
  public Result execute(Environment environment) {
    Result result = new Result();
    int admin = _admin.evaluate(environment);
    environment.getOutputRoute().setAdmin(admin);
    if (environment.getWriteToIntermediateBgpAttributes()) {
      environment.getIntermediateBgpAttributes().setAdmin(admin);
    }
    return result;
  }

  @JsonProperty(PROP_ADMIN)
  @Nonnull
  public IntExpr getAdmin() {
    return _admin;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _admin.hashCode();
    return result;
  }
}
