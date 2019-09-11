package org.batfish.datamodel.routing_policy.communities;

/** A visitor of {@link CommunitySetRendering}. */
public interface CommunitySetRenderingVisitor<T> {

  T visitTypesFirstAscendingSpaceSeparated(
      TypesFirstAscendingSpaceSeparated typesFirstAscendingSpaceSeparated);
}
