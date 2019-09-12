package org.batfish.datamodel.routing_policy.communities;

/** A visitor of {@link CommunitySetRendering}. */
public interface CommunitySetRenderingVisitor<T, U> {

  T visitTypesFirstAscendingSpaceSeparated(
      TypesFirstAscendingSpaceSeparated typesFirstAscendingSpaceSeparated, U arg);
}
