package org.batfish.datamodel.routing_policy.statement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.SortedSet;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpRoute;
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

    /** */
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
      Result result = new Result();
      switch (this._type) {
        case DefaultAction:
          result.setExit(true);
          result.setBooleanValue(environment.getDefaultAction());
          break;

        case DeleteAllCommunities:
          break;

        case ExitAccept:
          result.setExit(true);
          result.setBooleanValue(true);
          break;

        case ExitReject:
          result.setExit(true);
          result.setBooleanValue(false);
          break;

        case FallThrough:
          result.setReturn(true);
          result.setFallThrough(true);
          break;

        case RemovePrivateAs:
          {
            BgpRoute.Builder bgpRouteBuilder = (BgpRoute.Builder) environment.getOutputRoute();
            List<SortedSet<Long>> newAsPath = AsPath.removePrivateAs(bgpRouteBuilder.getAsPath());
            bgpRouteBuilder.setAsPath(newAsPath);
            if (environment.getWriteToIntermediateBgpAttributes()) {
              BgpRoute.Builder ir = environment.getIntermediateBgpAttributes();
              List<SortedSet<Long>> iAsPath = AsPath.removePrivateAs(ir.getAsPath());
              ir.setAsPath(iAsPath);
            }
            break;
          }

        case Return:
          result.setReturn(true);
          break;

        case ReturnFalse:
          result.setReturn(true);
          result.setBooleanValue(false);
          break;

        case ReturnLocalDefaultAction:
          result.setReturn(true);
          result.setBooleanValue(environment.getLocalDefaultAction());
          break;

        case ReturnTrue:
          result.setReturn(true);
          result.setBooleanValue(true);
          break;

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
          if (environment.getIntermediateBgpAttributes() == null) {
            BgpRoute.Builder ir = new BgpRoute.Builder();
            environment.setIntermediateBgpAttributes(ir);
            AbstractRoute or = environment.getOriginalRoute();
            ir.setMetric(or.getMetric());
            ir.setTag(or.getTag());
          }
          environment.setWriteToIntermediateBgpAttributes(true);
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
      return result;
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
