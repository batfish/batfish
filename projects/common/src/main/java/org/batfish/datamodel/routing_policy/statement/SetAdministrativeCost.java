package org.batfish.datamodel.routing_policy.statement;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.AdministrativeCostExpr;

/**
 * Type of {@link Statement} to set the administrative cost of output route present in the {@link
 * Environment}
 */
@ParametersAreNonnullByDefault
public final class SetAdministrativeCost extends Statement {
  private static final String PROP_ADMIN = "admin";

  private final @Nonnull AdministrativeCostExpr _admin;

  @JsonCreator
  private static SetAdministrativeCost jsonCreator(
      @JsonProperty(PROP_ADMIN) @Nullable AdministrativeCostExpr admin) {
    checkArgument(admin != null, "%s must be provided", PROP_ADMIN);
    return new SetAdministrativeCost(admin);
  }

  public SetAdministrativeCost(AdministrativeCostExpr admin) {
    _admin = admin;
  }

  @Override
  public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
    return visitor.visitSetAdministrativeCost(this, arg);
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
    long admin = _admin.evaluate(environment);
    environment.getOutputRoute().setAdmin(admin);
    if (environment.getWriteToIntermediateBgpAttributes()) {
      environment.getIntermediateBgpAttributes().setAdmin(admin);
    }
    return result;
  }

  @JsonProperty(PROP_ADMIN)
  public @Nonnull AdministrativeCostExpr getAdmin() {
    return _admin;
  }

  @Override
  public int hashCode() {
    return _admin.hashCode();
  }
}
