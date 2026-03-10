package org.batfish.question.searchfilters;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.datamodel.IpAccessList;

/** {@link SearchFiltersQuery} for finding permitted flows */
@ParametersAreNonnullByDefault
public class PermitQuery implements SearchFiltersQuery {
  public static final PermitQuery INSTANCE = new PermitQuery();

  private PermitQuery() {}

  @Override
  public boolean canQuery(IpAccessList acl) {
    return true;
  }

  @Override
  public @Nonnull BDD getMatchingBdd(
      IpAccessList acl, IpAccessListToBdd ipAccessListToBdd, BDDPacket pkt) {
    return ipAccessListToBdd.toBdd(acl);
  }
}
