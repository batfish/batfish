package org.batfish.datamodel.routing_policy.communities;

import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.CommunityVisitor;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;

/** Visitor for rendering a {@link Community} given a {@link CommunityRendering}. */
public final class CommunityRenderer implements CommunityRenderingVisitor<String> {

  public CommunityRenderer(Community community) {
    _community = community;
  }

  @Override
  public String visitColonSeparatedRendering(ColonSeparatedRendering colonSeparatedRendering) {
    return _community.accept(
        new CommunityVisitor<String>() {
          @Override
          public String visitExtendedCommunity(ExtendedCommunity extendedCommunity) {
            // TODO: implement reasonably
            return "";
          }

          @Override
          public String visitLargeCommunity(LargeCommunity largeCommunity) {
            return String.format(
                "%s:%s:%s",
                Long.toString(largeCommunity.getGlobalAdministrator()),
                Long.toString(largeCommunity.getLocalData1()),
                Long.toString(largeCommunity.getLocalData2()));
          }

          @Override
          public String visitStandardCommunity(StandardCommunity standardCommunity) {
            return standardCommunity.toString();
          }
        });
  }

  @Override
  public String visitIntegerValueRendering(IntegerValueRendering integerValueRendering) {
    return _community.accept(
        new CommunityVisitor<String>() {
          @Override
          public String visitExtendedCommunity(ExtendedCommunity extendedCommunity) {
            return extendedCommunity.asBigInt().toString();
          }

          @Override
          public String visitLargeCommunity(LargeCommunity largeCommunity) {
            return largeCommunity.asBigInt().toString();
          }

          @Override
          public String visitStandardCommunity(StandardCommunity standardCommunity) {
            return Long.toString(standardCommunity.asLong());
          }
        });
  }

  private final @Nonnull Community _community;
}
