package org.batfish.datamodel.acl;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;

public final class AclLineMatchExprs {
  private AclLineMatchExprs() {}

  public static final FalseExpr FALSE = FalseExpr.INSTANCE;

  public static final OriginatingFromDevice ORIGINATING_FROM_DEVICE =
      OriginatingFromDevice.INSTANCE;

  public static final TrueExpr TRUE = TrueExpr.INSTANCE;

  public static AndMatchExpr and(AclLineMatchExpr... exprs) {
    return and(Arrays.asList(exprs));
  }

  public static AndMatchExpr and(List<AclLineMatchExpr> exprs) {
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

  public static MatchHeaderSpace matchDst(Prefix prefix) {
    return matchDst(prefix.toIpSpace());
  }

  public static MatchHeaderSpace matchDstIp(String ip) {
    return matchDst(new Ip(ip).toIpSpace());
  }

  public static MatchSrcInterface matchSrcInterface(String... iface) {
    return new MatchSrcInterface(ImmutableList.copyOf(iface));
  }

  public static NotMatchExpr not(AclLineMatchExpr expr) {
    return new NotMatchExpr(expr);
  }

  public static OrMatchExpr or(AclLineMatchExpr... exprs) {
    return or(Arrays.asList(exprs));
  }

  public static OrMatchExpr or(List<AclLineMatchExpr> exprs) {
    return new OrMatchExpr(exprs);
  }

  public static PermittedByAcl permittedByAcl(String aclName) {
    return new PermittedByAcl(aclName);
  }
}
