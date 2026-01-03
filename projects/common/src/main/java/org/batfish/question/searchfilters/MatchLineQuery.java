package org.batfish.question.searchfilters;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.datamodel.IpAccessList;

/** {@link SearchFiltersQuery} for finding flows that match a particular line */
public class MatchLineQuery implements SearchFiltersQuery {
  private final int _lineNum;

  MatchLineQuery(int lineNum) {
    _lineNum = lineNum;
  }

  public int getLineNum() {
    return _lineNum;
  }

  @Override
  public boolean canQuery(IpAccessList acl) {
    return acl.getLines().size() > _lineNum;
  }

  @Override
  public @Nonnull BDD getMatchingBdd(
      IpAccessList acl, IpAccessListToBdd ipAccessListToBdd, BDDPacket pkt) {
    checkArgument(canQuery(acl), "ACL %s is too short to apply match line query", acl.getName());

    // Generate BDD matching all flows that would match the target line, then subtract out the BDD
    // of all flows matched by any previous line
    BDD matchingTargetLine =
        ipAccessListToBdd.toPermitAndDenyBdds(acl.getLines().get(_lineNum)).getMatchBdd();
    List<BDD> prevLineBdds =
        IntStream.range(0, _lineNum)
            .mapToObj(
                i -> ipAccessListToBdd.toPermitAndDenyBdds(acl.getLines().get(i)).getMatchBdd())
            .collect(ImmutableList.toImmutableList());
    return matchingTargetLine.diff(pkt.getFactory().orAll(prevLineBdds));
  }
}
