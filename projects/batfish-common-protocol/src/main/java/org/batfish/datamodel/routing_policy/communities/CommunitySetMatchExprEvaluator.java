package org.batfish.datamodel.routing_policy.communities;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import org.batfish.common.util.PatternProvider;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.routing_policy.expr.IntMatchExprEvaluator;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;

/** A visitor for evaluating a {@link CommunitySetMatchExpr} under a {@link CommunityContext}. */
public final class CommunitySetMatchExprEvaluator
    implements CommunitySetMatchExprVisitor<Boolean, CommunitySet> {

  public CommunitySetMatchExprEvaluator(CommunityContext ctx) {
    _ctx = ctx;
  }

  @Override
  public @Nonnull Boolean visitCommunitySetAcl(CommunitySetAcl communitySetAcl, CommunitySet arg) {
    for (CommunitySetAclLine line : communitySetAcl.getLines()) {
      if (line.getCommunitySetMatchExpr().accept(this, arg)) {
        return line.getAction() == LineAction.PERMIT;
      }
    }
    return false;
  }

  @Override
  public @Nonnull Boolean visitCommunitySetMatchAll(
      CommunitySetMatchAll communitySetMatchAll, CommunitySet arg) {
    for (CommunitySetMatchExpr expr : communitySetMatchAll.getExprs()) {
      if (!expr.accept(this, arg)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public @Nonnull Boolean visitCommunitySetMatchAny(
      CommunitySetMatchAny communitySetMatchAny, CommunitySet arg) {
    for (CommunitySetMatchExpr expr : communitySetMatchAny.getExprs()) {
      if (expr.accept(this, arg)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public @Nonnull Boolean visitCommunitySetMatchExprReference(
      CommunitySetMatchExprReference communitySetMatchExprReference, CommunitySet arg) {
    CommunitySetMatchExpr expr =
        _ctx.getCommunitySetMatchExprs().get(communitySetMatchExprReference.getName());
    // conversion to VI should guarantee expr is not null
    assert expr != null;
    return expr.accept(this, arg);
  }

  @Override
  public @Nonnull Boolean visitCommunitySetMatchRegex(
      CommunitySetMatchRegex communitySetMatchRegex, CommunitySet arg) {
    Boolean matches = REGEX_MATCH_CACHE.get(new RegexCacheKey(communitySetMatchRegex, arg));
    assert matches != null; // evaluator can't return null
    return matches;
  }

  @Override
  public @Nonnull Boolean visitCommunitySetNot(CommunitySetNot communitySetNot, CommunitySet arg) {
    return !communitySetNot.getExpr().accept(this, arg);
  }

  @Override
  public @Nonnull Boolean visitHasCommunity(HasCommunity hasCommunity, CommunitySet arg) {
    for (Community c : arg.getCommunities()) {
      if (hasCommunity.getExpr().accept(_ctx.getCommunityMatchExprEvaluator(), c)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public @Nonnull Boolean visitHasSize(HasSize hasSize, CommunitySet arg) {
    int actual = arg.getCommunities().size();
    IntMatchExprEvaluator eval = IntMatchExprEvaluator.instance();
    return hasSize.getExpr().accept(eval, new LiteralInt(actual));
  }

  private final @Nonnull CommunityContext _ctx;

  ////////////////////////////////
  private static final LoadingCache<RegexCacheKey, Boolean> REGEX_MATCH_CACHE =
      Caffeine.newBuilder()
          .maximumSize(1 << 20) // 1M instances that are each using maybe 40 bytes
          .build(
              k ->
                  PatternProvider.fromString(k._regex.getRegex())
                      .matcher(
                          k._regex
                              .getCommunitySetRendering()
                              .accept(CommunitySetToRegexInputString.instance(), k._set))
                      .find());

  @VisibleForTesting
  static final class RegexCacheKey {
    public RegexCacheKey(CommunitySetMatchRegex regex, CommunitySet set) {
      _regex = regex;
      _set = set;
      _hashCode = 31 * regex.hashCode() + set.hashCode(); // inlined hash
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof RegexCacheKey)) {
        return false;
      }
      RegexCacheKey that = (RegexCacheKey) o;
      return _hashCode == that._hashCode && _regex.equals(that._regex) && _set.equals(that._set);
    }

    @Override
    public int hashCode() {
      return _hashCode;
    }

    private final CommunitySetMatchRegex _regex;
    private final CommunitySet _set;
    private final int _hashCode;
  }
}
