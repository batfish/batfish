package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.BgpRoute.Builder;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public enum Statements {
  DefaultAction,
  DeleteAllCommunities,
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

    private static final long serialVersionUID = 1L;

    private Statements _type;

    @JsonCreator
    public StaticStatement(@JsonProperty(PROP_TYPE) Statements type) {
      _type = type;
    }

    @Override
    public boolean equals(Object rhs) {
      if (rhs instanceof StaticStatement) {
        return _type.equals(((StaticStatement) rhs)._type);
      }
      return false;
    }

    @Override
    public Result execute(Environment environment) {
      switch (this._type) {
        case DefaultAction:
          return Result.builder()
              .setExit(true)
              .setBooleanValue(environment.getDefaultAction())
              .build();

        case DeleteAllCommunities:
          break;

        case ExitAccept:
          return Result.builder().setExit(true).setBooleanValue(true).build();

        case ExitReject:
          return Result.builder().setExit(true).setBooleanValue(false).build();

        case FallThrough:
          return Result.builder().setReturn(true).setFallThrough(true).build();

        case RemovePrivateAs:
          {
            BgpRoute.Builder<?, ?> bgpRouteBuilder =
                (BgpRoute.Builder<?, ?>) environment.getOutputRoute();
            bgpRouteBuilder.setAsPath(bgpRouteBuilder.getAsPath().removePrivateAs());
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
          environment.setReadFromIntermediateBgpAttributes(true);
          break;

        case SetWriteIntermediateBgpAttributes:
          if (environment.getOutputRoute() instanceof BgpRoute.Builder<?, ?>) {
            environment.setWriteToIntermediateBgpAttributes(true);
            if (environment.getIntermediateBgpAttributes() == null) {
              BgpRoute.Builder<?, ?> bgpRouteBuilder = (Builder<?, ?>) environment.getOutputRoute();
              AbstractRoute or = environment.getOriginalRoute();
              environment.setIntermediateBgpAttributes(
                  bgpRouteBuilder.newBuilder().setMetric(or.getMetric()).setTag(or.getTag()));
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

        default:
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
