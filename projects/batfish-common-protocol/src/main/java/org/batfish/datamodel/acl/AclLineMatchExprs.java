package org.batfish.datamodel.acl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

public final class AclLineMatchExprs {

  private AclLineMatchExprs() {}

  public static final FalseExpr FALSE = FalseExpr.INSTANCE;

  public static final OriginatingFromDevice ORIGINATING_FROM_DEVICE =
      OriginatingFromDevice.INSTANCE;

  public static final TrueExpr TRUE = TrueExpr.INSTANCE;

  public static AclLineMatchExpr and(AclLineMatchExpr... exprs) {
    return and(
        Arrays.stream(exprs)
            .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder())));
  }

  /**
   * Constant-time constructor for AndMatchExpr. Simplifies if given zero or one conjunct. Doesn't
   * do other simplifications that require more work (like removing all {@link TrueExpr TrueExprs}).
   */
  public static AclLineMatchExpr and(Iterable<AclLineMatchExpr> exprs) {
    Iterator<AclLineMatchExpr> iter = exprs.iterator();

    if (!iter.hasNext()) {
      // Empty. Return the identity element
      return TrueExpr.INSTANCE;
    }

    AclLineMatchExpr first = iter.next();
    if (!iter.hasNext()) {
      // Only 1 element
      return first;
    }

    return new AndMatchExpr(exprs);
  }

  public static MatchHeaderSpace match(HeaderSpace headerSpace) {
    return new MatchHeaderSpace(headerSpace);
  }

  public static MatchHeaderSpace matchDst(IpSpace ipSpace) {
    return new MatchHeaderSpace(HeaderSpace.builder().setDstIps(ipSpace).build());
  }

  public static MatchHeaderSpace matchDst(Ip ip) {
    return matchDst(ip.toIpSpace());
  }

  public static MatchHeaderSpace matchDst(IpWildcard wc) {
    return matchDst(wc.toIpSpace());
  }

  public static MatchHeaderSpace matchDst(Prefix prefix) {
    return matchDst(prefix.toIpSpace());
  }

  public static MatchHeaderSpace matchDstIp(String ip) {
    return matchDst(new Ip(ip).toIpSpace());
  }

  public static AclLineMatchExpr matchDstPrefix(String prefix) {
    return matchDst(Prefix.parse(prefix));
  }

  public static MatchHeaderSpace matchSrc(IpSpace ipSpace) {
    return new MatchHeaderSpace(HeaderSpace.builder().setSrcIps(ipSpace).build());
  }

  public static MatchHeaderSpace matchSrc(Ip ip) {
    return matchSrc(ip.toIpSpace());
  }

  public static MatchHeaderSpace matchSrc(Prefix prefix) {
    return matchSrc(prefix.toIpSpace());
  }

  public static MatchHeaderSpace matchSrcIp(String ip) {
    return matchSrc(new Ip(ip).toIpSpace());
  }

  public static MatchHeaderSpace matchSrcPrefix(String prefix) {
    return matchSrc(Prefix.parse(prefix).toIpSpace());
  }

  public static MatchSrcInterface matchSrcInterface(String... iface) {
    return new MatchSrcInterface(ImmutableList.copyOf(iface));
  }

  /**
   * Smart constructor for {@link NotMatchExpr} that does constant-time simplifications (i.e. when
   * expr is {@link #TRUE} or {@link #FALSE}).
   */
  public static AclLineMatchExpr not(AclLineMatchExpr expr) {
    if (expr == TRUE) {
      return FALSE;
    }
    if (expr == FALSE) {
      return TRUE;
    }
    if (expr instanceof NotMatchExpr) {
      return ((NotMatchExpr) expr).getOperand();
    }
    return new NotMatchExpr(expr);
  }

  public static AclLineMatchExpr or(AclLineMatchExpr... exprs) {
    return or(
        Arrays.stream(exprs).collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural())));
  }

  /**
   * Constant-time constructor for OrMatchExpr. Simplifies if given zero or one conjunct. Doesn't do
   * other simplifications that require more work (like removing all {@link FalseExpr FalseExprs}).
   */
  public static AclLineMatchExpr or(Iterable<AclLineMatchExpr> exprs) {
    Iterator<AclLineMatchExpr> iter = exprs.iterator();
    if (!iter.hasNext()) {
      // Empty. Return the identity element.
      return FalseExpr.INSTANCE;
    }
    AclLineMatchExpr first = iter.next();
    if (!iter.hasNext()) {
      // Only 1 element
      return first;
    }
    return new OrMatchExpr(exprs);
  }

  public static PermittedByAcl permittedByAcl(String aclName) {
    return new PermittedByAcl(aclName);
  }
}
