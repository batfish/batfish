package org.batfish.datamodel.routing_policy.communities;

/** Visitor of a {@link CommunityRendering}. */
public interface CommunityRenderingVisitor<T> {

  T visitColonSeparatedRendering(ColonSeparatedRendering colonSeparatedRendering);

  T visitIntegerValueRendering(IntegerValueRendering integerValueRendering);
}
