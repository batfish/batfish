package org.batfish.datamodel.routing_policy.communities;

/** Visitor of a {@link CommunityRendering}. */
public interface CommunityRenderingVisitor<T, U> {

  T visitColonSeparatedRendering(ColonSeparatedRendering colonSeparatedRendering, U arg);

  T visitIntegerValueRendering(IntegerValueRendering integerValueRendering, U arg);
}
