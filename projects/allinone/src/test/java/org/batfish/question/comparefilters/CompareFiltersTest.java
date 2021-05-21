package org.batfish.question.comparefilters;

import static org.batfish.datamodel.ExprAclLine.accepting;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.util.SortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class CompareFiltersTest {
  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testMatchSrcInterface_noDifference() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();
    Interface iface = nf.interfaceBuilder().setOwner(c).setVrf(vrf).setActive(true).build();
    nf.aclBuilder()
        .setOwner(c)
        .setName("test")
        .setLines(accepting(matchSrcInterface(iface.getName())))
        .build();

    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(c.getHostname(), c);

    // create a differential environment with equal current and reference snapshots.
    Batfish batfish = BatfishTestUtils.getBatfish(configs, configs, _folder);

    CompareFiltersQuestion question = new CompareFiltersQuestion(null, null, null);
    TableAnswerElement answer =
        (TableAnswerElement)
            new CompareFiltersAnswerer(question, batfish)
                .answerDiff(batfish.getSnapshot(), batfish.getReferenceSnapshot());

    assertThat(answer.getRowsList(), empty());
  }
}
