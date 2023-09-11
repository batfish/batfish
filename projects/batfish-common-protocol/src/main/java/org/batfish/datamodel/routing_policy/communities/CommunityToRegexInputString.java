package org.batfish.datamodel.routing_policy.communities;

import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.CommunityVisitor;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;

/** Visitor for rendering a {@link Community} given a {@link CommunityRendering}. */
public final class CommunityToRegexInputString
    implements CommunityRenderingVisitor<String, Community> {

  public static @Nonnull CommunityToRegexInputString instance() {
    return INSTANCE;
  }

  @Override
  public String visitColonSeparatedRendering(
      ColonSeparatedRendering colonSeparatedRendering, Community arg) {
    return arg.accept(COLON_SEPARATED_RENDERER);
  }

  @Override
  public String visitIntegerValueRendering(
      IntegerValueRendering integerValueRendering, Community arg) {
    return arg.accept(INTEGER_VALUE_RENDERER);
  }

  @Override
  public String visitSpecialCasesRendering(
      SpecialCasesRendering specialCasesRendering, Community arg) {
    String ret = specialCasesRendering.getSpecialCases().get(arg);
    return ret != null ? ret : visit(specialCasesRendering.getFallbackRendering(), arg);
  }

  private static final class ColonSeparatedRenderer implements CommunityVisitor<String> {
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
  }

  private static final class IntegerValueRenderer implements CommunityVisitor<String> {
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
  }

  private static final ColonSeparatedRenderer COLON_SEPARATED_RENDERER =
      new ColonSeparatedRenderer();
  private static final IntegerValueRenderer INTEGER_VALUE_RENDERER = new IntegerValueRenderer();
  private static final CommunityToRegexInputString INSTANCE = new CommunityToRegexInputString();

  private CommunityToRegexInputString() {}
}
