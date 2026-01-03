package org.batfish.datamodel.routing_policy.communities;

import java.util.Comparator;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.CommunityVisitor;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;

/** A {@link CommunitySetRenderingVisitor} that renders a {@link CommunitySet} as a string. */
public final class CommunitySetToRegexInputString
    implements CommunitySetRenderingVisitor<String, CommunitySet> {

  public static @Nonnull CommunitySetToRegexInputString instance() {
    return INSTANCE;
  }

  @Override
  public String visitTypesFirstAscendingSpaceSeparated(
      TypesFirstAscendingSpaceSeparated typesFirstAscendingSpaceSeparated, CommunitySet arg) {
    return arg.getCommunities().stream()
        .sorted(TYPES_FIRST_ASCENDING_COMPARATOR)
        .map(
            c ->
                typesFirstAscendingSpaceSeparated
                    .getCommunityRendering()
                    .accept(CommunityToRegexInputString.instance(), c))
        .collect(Collectors.joining(" "));
  }

  private static final class CommunityPriority implements CommunityVisitor<Integer> {
    @Override
    public Integer visitExtendedCommunity(ExtendedCommunity extendedCommunity) {
      return 1;
    }

    @Override
    public Integer visitLargeCommunity(LargeCommunity largeCommunity) {
      return 2;
    }

    @Override
    public Integer visitStandardCommunity(StandardCommunity standardCommunity) {
      return 0;
    }
  }

  private static final CommunityPriority COMMUNITY_PRIORITY = new CommunityPriority();
  private static final Comparator<Community> TYPES_FIRST_ASCENDING_COMPARATOR =
      Comparator.<Community, Integer>comparing(c -> c.accept(COMMUNITY_PRIORITY))
          .thenComparing(Community::asBigInt);
  private static final CommunitySetToRegexInputString INSTANCE =
      new CommunitySetToRegexInputString();

  private CommunitySetToRegexInputString() {}
}
