package org.batfish.bddreachability.transition;

/** Visitor interface for {@link Transition} */
public interface TransitionVisitor<T> {
  default T visit(Transition transition) {
    return transition.accept(this);
  }

  T visitAddLastHopConstraint(AddLastHopConstraint addLastHopConstraint);

  T visitAddNoLastHopConstraint(AddNoLastHopConstraint addNoLastHopConstraint);

  T visitAddOutgoingOriginalFlowFiltersConstraint(
      AddOutgoingOriginalFlowFiltersConstraint addOutgoingOriginalFlowFiltersConstraint);

  T visitAddSourceConstraint(AddSourceConstraint addSourceConstraint);

  T visitComposite(Composite composite);

  T visitConstraint(Constraint constraint);

  T visitEraseAndSet(EraseAndSet eraseAndSet);

  T visitIdentity(Identity identity);

  T visitOr(Or or);

  T visitRemoveLastHopConstraint(RemoveLastHopConstraint removeLastHopConstraint);

  T visitRemoveOutgoingInterfaceConstraints(
      RemoveOutgoingInterfaceConstraints removeOutgoingInterfaceConstraints);

  T visitRemoveSourceConstraint(RemoveSourceConstraint removeSourceConstraint);

  T visitReverse(Reverse reverse);

  T visitTransform(Transform transform);

  T visitZero(Zero zero);
}
