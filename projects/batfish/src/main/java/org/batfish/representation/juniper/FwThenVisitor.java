package org.batfish.representation.juniper;

/** A visitor that helps evaluate (or convert) {@link FwThen} statements. */
public interface FwThenVisitor<T> {

  default T visit(FwThen then) {
    return then.accept(this);
  }

  T visitFwThenAccept(FwThenAccept accept);

  T visitFwThenDiscard(FwThenDiscard discard);

  T visitFwThenNextIp(FwThenNextIp nextIp);

  T visitFwThenNextTerm(FwThenNextTerm accept);

  T visitFwThenNop(FwThenNop nop);

  T visitFwThenPolicer(FwThenPolicer policer);

  T visitThenRoutingInstance(FwThenRoutingInstance routingInstance);
}
