package org.batfish.datamodel.routing_policy.communities;

/** Visitor of a {@link CommunityRendering}. */
public interface CommunityRenderingVisitor<T, U> {

  default T visit(CommunityRendering communityRendering, U arg) {
    return communityRendering.accept(this, arg);
  }

  T visitColonSeparatedRendering(ColonSeparatedRendering colonSeparatedRendering, U arg);

  T visitIntegerValueRendering(IntegerValueRendering integerValueRendering, U arg);

  T visitSpecialCasesRendering(SpecialCasesRendering specialCasesRendering, U arg);
}
