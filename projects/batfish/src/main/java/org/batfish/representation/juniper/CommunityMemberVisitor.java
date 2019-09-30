package org.batfish.representation.juniper;

import javax.annotation.ParametersAreNonnullByDefault;

/** A visitor of {@link CommunityMember} that returns a generic value. */
@ParametersAreNonnullByDefault
public interface CommunityMemberVisitor<T> {

  T visitLiteralCommunityMember(LiteralCommunityMember literalCommunityMember);

  T visitRegexCommunityMember(RegexCommunityMember regexCommunityMember);
}
