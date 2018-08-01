package org.batfish.question.reducedreachability;

import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.batfish.datamodel.matchers.RowMatchers.hasColumn;
import static org.batfish.datamodel.matchers.TableAnswerElementMatchers.hasRows;
import static org.batfish.main.BatfishTestUtils.getBatfish;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.SortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.main.Batfish;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ReducedReachabilityQuestionTest {
  private static final String LOOPBACK = "Loopback0";
  private static final String NODE1 = "node1";
  private static final String NODE2 = "node2";
  private static final String PHYSICAL = "FastEthernet0/0";

  private Configuration.Builder _cb;

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private org.batfish.datamodel.Interface.Builder _ib;

  private NetworkFactory _nf;

  private org.batfish.datamodel.Vrf.Builder _vb;

  public ReducedReachabilityQuestionTest() {}

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(CISCO_IOS);
    _vb = _nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
    _ib = _nf.interfaceBuilder().setActive(true);
  }

  private SortedMap<String, Configuration> generateConfigs(boolean delta) {
    Configuration node1 = _cb.setHostname(NODE1).build();
    Vrf v1 = _vb.setOwner(node1).build();
    _ib.setOwner(node1).setVrf(v1);
    _ib.setName(LOOPBACK).setAddresses(new InterfaceAddress("1.1.1.1/32")).build();
    _ib.setName(PHYSICAL).setAddresses(new InterfaceAddress("1.1.1.2/31")).build();
    if (!delta) {
      v1.setStaticRoutes(
          ImmutableSortedSet.of(
              StaticRoute.builder()
                  .setNetwork(Prefix.parse("2.2.2.2/32"))
                  .setNextHopInterface(PHYSICAL)
                  .build()));
    }

    Configuration node2 = _cb.setHostname(NODE2).build();
    Vrf v2 = _vb.setOwner(node2).build();
    _ib.setOwner(node2).setVrf(v2);
    _ib.setName(PHYSICAL)
        .setAddresses(new InterfaceAddress("1.1.1.3/31"), new InterfaceAddress("2.2.2.2/32"))
        .build();
    v2.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNetwork(Prefix.parse("1.1.1.1/32"))
                .setNextHopInterface(PHYSICAL)
                .build()));

    return ImmutableSortedMap.of(NODE1, node1, NODE2, node2);
  }

  private Batfish initBatfish() throws IOException {
    SortedMap<String, Configuration> baseConfigs = generateConfigs(false);
    SortedMap<String, Configuration> deltaConfigs = generateConfigs(true);
    Batfish batfish = getBatfish(baseConfigs, deltaConfigs, _folder);

    batfish.pushBaseEnvironment();
    batfish.computeDataPlane(true);
    batfish.popEnvironment();

    batfish.pushDeltaEnvironment();
    batfish.computeDataPlane(true);
    batfish.popEnvironment();

    return batfish;
  }

  @Test
  public void testReducedReachabilityQuestion() throws IOException {
    Question question = new ReducedReachabilityQuestion();
    Batfish batfish = initBatfish();
    TableAnswerElement answer =
        (TableAnswerElement) new ReducedReachabilityAnswerer(question, batfish).answer();
    Node node = new Node("node1");
    Ip dstIp = new Ip("2.2.2.2");
    assertThat(
        answer,
        hasRows(
            contains(
                ImmutableList.of(
                    allOf(
                        hasColumn(
                            ReducedReachabilityAnswerer.COL_ENVIRONMENT,
                            equalTo("BASE"),
                            Schema.STRING),
                        hasColumn(ReducedReachabilityAnswerer.COL_NODE, equalTo(node), Schema.NODE),
                        hasColumn(
                            ReducedReachabilityAnswerer.COL_DST_IP, equalTo(dstIp), Schema.IP),
                        hasColumn(
                            ReducedReachabilityAnswerer.COL_RESULTS,
                            equalTo(ImmutableList.of("ACCEPTED")),
                            Schema.OBJECT)),
                    allOf(
                        hasColumn(
                            ReducedReachabilityAnswerer.COL_ENVIRONMENT,
                            equalTo("DELTA"),
                            Schema.STRING),
                        hasColumn(ReducedReachabilityAnswerer.COL_NODE, equalTo(node), Schema.NODE),
                        hasColumn(
                            ReducedReachabilityAnswerer.COL_DST_IP, equalTo(dstIp), Schema.IP),
                        hasColumn(
                            ReducedReachabilityAnswerer.COL_RESULTS,
                            equalTo(ImmutableList.of("NO_ROUTE")),
                            Schema.OBJECT))))));
  }
}
