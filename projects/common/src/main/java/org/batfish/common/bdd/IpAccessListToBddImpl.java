package org.batfish.common.bdd;

import java.util.Map;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;

/** Implementation of {@link IpAccessListToBdd}. */
public final class IpAccessListToBddImpl extends IpAccessListToBdd {
  public IpAccessListToBddImpl(
      @Nonnull BDDPacket pkt,
      @Nonnull BDDSourceManager bddSrcManager,
      @Nonnull Map<String, IpAccessList> aclEnv,
      @Nonnull Map<String, IpSpace> ipSpaceEnv) {
    super(pkt, bddSrcManager, aclEnv, ipSpaceEnv);
  }

  public IpAccessListToBddImpl(
      @Nonnull BDDPacket pkt,
      @Nonnull BDDSourceManager bddSrcManager,
      @Nonnull HeaderSpaceToBDD headerSpaceToBDD,
      @Nonnull Map<String, IpAccessList> aclEnv) {
    super(pkt, bddSrcManager, headerSpaceToBDD, aclEnv);
  }

  @Override
  public PermitAndDenyBdds toPermitAndDenyBdds(AclLine line) {
    return convert(line);
  }

  @Override
  public BDD toBdd(AclLineMatchExpr expr) {
    return convert(expr);
  }
}
