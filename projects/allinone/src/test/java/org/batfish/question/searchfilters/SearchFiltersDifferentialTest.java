package org.batfish.question.searchfilters;

import static org.batfish.datamodel.IpAccessListLine.accepting;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.datamodel.matchers.FlowMatchers.hasDstIp;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.datamodel.matchers.TableAnswerElementMatchers.hasRows;
import static org.batfish.question.testfilters.TestFiltersAnswerer.COL_ACTION;
import static org.batfish.question.testfilters.TestFiltersAnswerer.COL_FLOW;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableDiff;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** End-to-end tests of {@link SearchFiltersQuestion} in differential mode. */
public class SearchFiltersDifferentialTest {
  @Rule public TemporaryFolder _tmp = new TemporaryFolder();

  private static final String HOSTNAME = "hostname";
  private static final String ACLNAME = "acl";
  private static final String IFACE = "iface";

  private NetworkFactory _nf;
  private Configuration.Builder _cb;
  private Interface.Builder _ib;
  private IpAccessList.Builder _ab;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb =
        _nf.configurationBuilder()
            .setHostname(HOSTNAME)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _ib = _nf.interfaceBuilder().setName(IFACE);
    _ab = _nf.aclBuilder().setName(ACLNAME);
  }

  private Batfish getBatfish(Configuration baseConfig, Configuration deltaConfig)
      throws IOException {
    return BatfishTestUtils.getBatfish(
        ImmutableSortedMap.of(baseConfig.getHostname(), baseConfig),
        ImmutableSortedMap.of(deltaConfig.getHostname(), deltaConfig),
        _tmp);
  }

  @Test
  public void testAclLineAddedRemoved() throws IOException {
    Ip ip = new Ip("1.2.3.4");
    Configuration baseConfig = _cb.build();
    Configuration deltaConfig = _cb.build();
    _ib.setOwner(baseConfig).build();
    _ib.setOwner(deltaConfig).build();
    _ab.setOwner(baseConfig).build();
    _ab.setOwner(deltaConfig)
        .setLines(
            ImmutableList.of(
                accepting().setMatchCondition(and(matchSrcInterface(IFACE), matchDst(ip))).build()))
        .build();
    Batfish batfish = getBatfish(baseConfig, deltaConfig);
    TableAnswerElement answer =
        (TableAnswerElement)
            new SearchFiltersAnswerer(
                    SearchFiltersQuestion.builder().setStartLocation("enter(.*)").build(), batfish)
                .answerDiff();
    assertThat(
        answer,
        hasRows(
            contains(
                ImmutableList.of(
                    allOf(
                        hasColumn(equalTo(COL_FLOW), hasDstIp(ip), Schema.FLOW),
                        hasColumn(
                            equalTo(TableDiff.baseColumnName(COL_ACTION)),
                            equalTo(LineAction.DENY.toString()),
                            Schema.STRING),
                        hasColumn(
                            equalTo(TableDiff.deltaColumnName(COL_ACTION)),
                            equalTo(LineAction.PERMIT.toString()),
                            Schema.STRING))))));
  }
}
