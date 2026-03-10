package org.batfish.question.searchfilters;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.datamodel.IpAccessList;

/** {@link SearchFiltersQuery} for finding denied flows */
@ParametersAreNonnullByDefault
public class DenyQuery implements SearchFiltersQuery {
  public static final DenyQuery INSTANCE = new DenyQuery();

  private DenyQuery() {}

  @Override
  public boolean canQuery(IpAccessList acl) {
    return true;
  }

  @Override
  public @Nonnull BDD getMatchingBdd(
      IpAccessList acl, IpAccessListToBdd ipAccessListToBdd, BDDPacket pkt) {
    return ipAccessListToBdd.toBdd(acl).not();
  }
}
