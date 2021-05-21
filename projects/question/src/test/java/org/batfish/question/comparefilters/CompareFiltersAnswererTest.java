package org.batfish.question.comparefilters;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.ExprAclLine.accepting;
import static org.batfish.datamodel.LineAction.DENY;
import static org.batfish.datamodel.LineAction.PERMIT;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.question.comparefilters.CompareFiltersAnswerer.compareFilters;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.PermitAndDenyBdds;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class CompareFiltersAnswererTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static final String HOSTNAME = "hostname";
  private static final String FILTER = "filter";
  private final BDDPacket _pkt = new BDDPacket();
  private final BDD _zero = _pkt.getFactory().zero();

  private List<FilterDifference> compare(
      List<LineAction> currentActions,
      List<BDD> currentBdds,
      List<LineAction> referenceActions,
      List<BDD> referenceBdds) {
    List<PermitAndDenyBdds> currentLineBdds = toPermitAndDenyBdds(currentActions, currentBdds);
    List<PermitAndDenyBdds> refLineBdds = toPermitAndDenyBdds(referenceActions, referenceBdds);
    return compare(currentLineBdds, refLineBdds);
  }

  private List<FilterDifference> compare(
      List<PermitAndDenyBdds> currentBdds, List<PermitAndDenyBdds> referenceBdds) {
    return compareFilters(HOSTNAME, FILTER, currentBdds, referenceBdds, _pkt)
        .collect(Collectors.toList());
  }

  private static FilterDifference difference(
      @Nullable Integer currentIndex, @Nullable Integer referenceIndex) {
    return new FilterDifference(HOSTNAME, FILTER, currentIndex, referenceIndex);
  }

  private List<BDD> aclBdds(BDD... lineBdds) {
    checkArgument(lineBdds.length > 0);

    ImmutableList.Builder<BDD> aclBdds = ImmutableList.builder();
    BDD reach = _pkt.getFactory().one();
    for (BDD lineBdd : lineBdds) {
      aclBdds.add(reach.and(lineBdd));
      reach = reach.and(lineBdd.not());
    }
    // add the BDD for flows that match no lines in the acl.
    aclBdds.add(reach);
    return aclBdds.build();
  }

  @Test
  public void testCompareFilters() {
    BDD dstIp0 = _pkt.getDstIp().value(0);
    BDD dstIp1 = _pkt.getDstIp().value(1);
    BDD srcIp0 = _pkt.getSrcIp().value(0);
    BDD dstPort0 = _pkt.getDstPort().value(0);
    BDD srcPort0 = _pkt.getSrcPort().value(0);

    List<BDD> zeroBdds = aclBdds(dstIp0, srcIp0, dstPort0, srcPort0);

    // Representation of an empty Acl.
    List<BDD> emptyAclBdds = ImmutableList.of(_pkt.getFactory().one());
    List<LineAction> emptyAclActions = ImmutableList.of();

    List<LineAction> pppp = ImmutableList.of(PERMIT, PERMIT, PERMIT, PERMIT);
    List<LineAction> dpdp = ImmutableList.of(DENY, PERMIT, DENY, PERMIT);

    // If nothing has changed, return no differences
    assertThat(compare(pppp, zeroBdds, pppp, zeroBdds), empty());

    // If all lines deleted, return a difference for each permit line
    assertThat(
        compare(emptyAclActions, emptyAclBdds, pppp, zeroBdds),
        contains(
            difference(null, 0), difference(null, 1), difference(null, 2), difference(null, 3)));
    assertThat(
        compare(emptyAclActions, emptyAclBdds, dpdp, zeroBdds),
        contains(difference(null, 1), difference(null, 3)));

    // If all lines added, return a difference for each permit line
    assertThat(
        compare(pppp, zeroBdds, emptyAclActions, emptyAclBdds),
        contains(
            difference(0, null), difference(1, null), difference(2, null), difference(3, null)));
    assertThat(
        compare(dpdp, zeroBdds, emptyAclActions, emptyAclBdds),
        contains(difference(1, null), difference(3, null)));

    {
      List<BDD> currentAcl = zeroBdds;
      List<BDD> referenceAcl = aclBdds(dstIp1, srcIp0, dstPort0, srcPort0);

      // if we change a permit line, we get differences for that line in each direction
      assertThat(
          compare(pppp, currentAcl, pppp, referenceAcl),
          contains(difference(0, null), difference(null, 0)));

      // if we change a deny line, we affect later permit lines
      List<FilterDifference> compare = compare(dpdp, currentAcl, dpdp, referenceAcl);
      assertThat(
          compare,
          contains(difference(0, 1), difference(0, 3), difference(1, 0), difference(3, 0)));
    }
  }

  private List<PermitAndDenyBdds> toPermitAndDenyBdds(List<LineAction> actions, List<BDD> bdds) {
    // bdds should have one more element than actions, representing the default denied packets
    assert bdds.size() == actions.size() + 1;
    return IntStream.range(0, bdds.size())
        .mapToObj(
            i -> {
              LineAction action = i == actions.size() ? DENY : actions.get(i);
              return action == PERMIT
                  ? new PermitAndDenyBdds(bdds.get(i), _zero)
                  : new PermitAndDenyBdds(_zero, bdds.get(i));
            })
        .collect(ImmutableList.toImmutableList());
  }

  @Test
  public void testMatchSrcInterface_noDifference() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();
    Interface iface = nf.interfaceBuilder().setOwner(c).setVrf(vrf).setActive(true).build();
    IpAccessList acl =
        nf.aclBuilder()
            .setOwner(c)
            .setName("test")
            .setLines(accepting(matchSrcInterface(iface.getName())))
            .build();

    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(c.getHostname(), c);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, configs, _folder);

    CompareFiltersQuestion question = new CompareFiltersQuestion(null, null, null);
    TableAnswerElement answer =
        (TableAnswerElement)
            new CompareFiltersAnswerer(question, batfish)
                .answerDiff(batfish.getSnapshot(), batfish.getReferenceSnapshot());

    assertThat(answer.getRowsList(), empty());
  }
}
