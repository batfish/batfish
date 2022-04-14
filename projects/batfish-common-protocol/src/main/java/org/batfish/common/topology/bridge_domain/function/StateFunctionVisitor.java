package org.batfish.common.topology.bridge_domain.function;

import org.batfish.common.topology.bridge_domain.function.FilterByOuterTag.FilterByOuterTagImpl;
import org.batfish.common.topology.bridge_domain.function.FilterByVlanId.FilterByVlanIdImpl;
import org.batfish.common.topology.bridge_domain.function.PopTag.PopTagImpl;
import org.batfish.common.topology.bridge_domain.function.TranslateVlan.TranslateVlanImpl;

/**
 * A visitor of {@link StateFunction} that takes a generic argument of type {@code U} and returns a
 * generic value of type {@code T}.
 */
public interface StateFunctionVisitor<T, U> {

  default T visit(StateFunction stateFunction, U arg) {
    return stateFunction.accept(this, arg);
  }

  T visitAssignVlanFromOuterTag(AssignVlanFromOuterTag assignVlanFromOuterTag, U arg);

  T visitClearVlanId(ClearVlanId clearVlanId, U arg);

  T visitCompose(ComposeBaseImpl<?> compose, U arg);

  T visitFilterByOuterTag(FilterByOuterTagImpl filterByOuterTag, U arg);

  T visitFilterByVlanId(FilterByVlanIdImpl filterByVlanId, U arg);

  T visitIdentity(Identity identity, U arg);

  T visitPopTag(PopTagImpl popTag, U arg);

  T visitPushTag(PushTag pushTag, U arg);

  T visitPushVlanId(PushVlanId pushVlanId, U arg);

  T visitSetVlanId(SetVlanId setVlanId, U arg);

  T visitTranslateVlan(TranslateVlanImpl translateVlan, U arg);
}
