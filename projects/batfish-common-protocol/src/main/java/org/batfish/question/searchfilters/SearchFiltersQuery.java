package org.batfish.question.searchfilters;

import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.datamodel.IpAccessList;

/** Query indicating which flows to search for (permitted, denied, or matching a certain line) */
public interface SearchFiltersQuery {

  /** Whether this query can be applied to the provided {@link IpAccessList} */
  boolean canQuery(IpAccessList acl);

  /**
   * Returns BDD representing the space of flows matching the given ACL for this query. Assumes this
   * query is applicable to the ACL as per {@link SearchFiltersQuery#canQuery(IpAccessList)}.
   */
  @Nonnull
  BDD getMatchingBdd(IpAccessList acl, IpAccessListToBdd ipAccessListToBdd, BDDPacket pkt);
}
