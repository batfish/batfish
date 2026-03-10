package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.BgpRoute.Builder;
import org.batfish.datamodel.HasReadableAsPath;
import org.batfish.datamodel.HasReadableCommunities;
import org.batfish.datamodel.HasWritableAsPath;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public enum Statements {
  DefaultAction,
  ExitAccept,
  ExitReject,
  FallThrough,
  RemovePrivateAs,
  Return,
  ReturnFalse,
  ReturnLocalDefaultAction,
  ReturnTrue,
  SetDefaultActionAccept,
  SetDefaultActionReject,
  SetLocalDefaultActionAccept,
  SetLocalDefaultActionReject,
  SetReadIntermediateBgpAttributes,
  SetWriteIntermediateBgpAttributes,
  Suppress,
  UnsetWriteIntermediateBgpAttributes,
  Unsuppress;

  public static class StaticStatement extends Statement {
    private static final String PROP_TYPE = "type";

    private Statements _type;

    @JsonCreator
    public StaticStatement(@JsonProperty(PROP_TYPE) Statements type) {
      _type = type;
    }

    @Override
    public <T, U> T accept(StatementVisitor<T, U> visitor, U arg) {
      return visitor.visitStaticStatement(this, arg);
    }

    @Override
    public boolean equals(Object rhs) {
      if (rhs instanceof StaticStatement) {
        return _type == ((StaticStatement) rhs)._type;
      }
      return false;
    }

    @Override
    public Result execute(Environment environment) {
      switch (_type) {
        case DefaultAction:
          return Result.builder()
              .setExit(true)
              .setBooleanValue(environment.getDefaultAction())
              .build();

        case ExitAccept:
          return Result.builder().setExit(true).setBooleanValue(true).build();

        case ExitReject:
          return Result.builder().setExit(true).setBooleanValue(false).build();

        case FallThrough:
          return Result.builder().setReturn(true).setFallThrough(true).build();

        case RemovePrivateAs:
          {
            if (!(environment.getOutputRoute() instanceof HasWritableAsPath)) {
              break;
            }
            HasWritableAsPath<?, ?> outputRoute =
                (HasWritableAsPath<?, ?>) environment.getOutputRoute();
            outputRoute.setAsPath(outputRoute.getAsPath().removePrivateAs());
            if (environment.getWriteToIntermediateBgpAttributes()) {
              BgpRoute.Builder<?, ?> ir = environment.getIntermediateBgpAttributes();
              ir.setAsPath(ir.getAsPath().removePrivateAs());
            }
            break;
          }

        case Return:
          return Result.builder().setReturn(true).build();

        case ReturnFalse:
          return Result.builder().setReturn(true).setBooleanValue(false).build();

        case ReturnLocalDefaultAction:
          return Result.builder()
              .setReturn(true)
              .setBooleanValue(environment.getLocalDefaultAction())
              .build();

        case ReturnTrue:
          return Result.builder().setReturn(true).setBooleanValue(true).build();

        case SetDefaultActionAccept:
          environment.setDefaultAction(true);
          break;

        case SetDefaultActionReject:
          environment.setDefaultAction(false);
          break;

        case SetLocalDefaultActionAccept:
          environment.setLocalDefaultAction(true);
          break;

        case SetLocalDefaultActionReject:
          environment.setLocalDefaultAction(false);
          break;

        case SetReadIntermediateBgpAttributes:
          if (environment.getOutputRoute() instanceof BgpRoute.Builder<?, ?>) {
            environment.setReadFromIntermediateBgpAttributes(true);
          }
          break;

        case SetWriteIntermediateBgpAttributes:
          if (environment.getOutputRoute() instanceof BgpRoute.Builder<?, ?>) {
            environment.setWriteToIntermediateBgpAttributes(true);
            if (environment.getIntermediateBgpAttributes() == null) {
              BgpRoute.Builder<?, ?> outputRouteBuilder =
                  (Builder<?, ?>) environment.getOutputRoute();
              AbstractRoute originalRoute = environment.getOriginalRoute();
              Builder<?, ?> intermediateBgpAttributes =
                  outputRouteBuilder
                      .newBuilder()
                      .setMetric(originalRoute.getMetric())
                      .setTag(originalRoute.getTag());
              if (originalRoute instanceof HasReadableAsPath) {
                intermediateBgpAttributes.setAsPath(
                    ((HasReadableAsPath) originalRoute).getAsPath());
              }
              if (originalRoute instanceof HasReadableCommunities) {
                intermediateBgpAttributes.setCommunities(
                    ((HasReadableCommunities) originalRoute).getCommunities());
              }
              environment.setIntermediateBgpAttributes(intermediateBgpAttributes);
            }
          }
          break;

        case Suppress:
          environment.setSuppressed(true);
          break;

        case UnsetWriteIntermediateBgpAttributes:
          environment.setWriteToIntermediateBgpAttributes(false);
          break;

        case Unsuppress:
          environment.setSuppressed(false);
          break;
      }
      return new Result(false);
    }

    @JsonProperty(PROP_TYPE)
    public Statements getType() {
      return _type;
    }

    @Override
    public int hashCode() {
      return _type.ordinal();
    }

    @Override
    public String toString() {
      return _type.toString();
    }
  }

  public StaticStatement toStaticStatement() {
    return new StaticStatement(this);
  }
}
