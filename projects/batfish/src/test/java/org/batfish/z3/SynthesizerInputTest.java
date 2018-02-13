package org.batfish.z3;

import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasEnabledFlowSinks;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasEnabledInterfaces;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasEnabledNodes;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasEnabledVrfs;
import static org.batfish.z3.matchers.SynthesizerInputMatchers.hasIpsByHostname;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.TestDataPlane;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.FibRow;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.z3.expr.FibRowMatchExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.matchers.SynthesizerInputMatchers;
import org.junit.Before;
import org.junit.Test;

public class SynthesizerInputTest {

  private Configuration.Builder _cb;

  private Configuration _cDisabled;

  private Configuration _cEnabled;

  private Interface.Builder _ib;

  private Interface _iDisabledViaBlacklisted;

  private Interface _iDisabledViaInactiveAndFlowSink;

  private Interface _iDisabledViaInterface;

  private Interface _iDisabledViaVrf;

  private Interface _iEnabled;

  private Interface _iEnabledAndFlowSink;

  private SynthesizerInput.Builder _inputBuilder;

  private NetworkFactory _nf;

  private Vrf.Builder _vb;

  private Vrf _vDisabledViaNode;

  private Vrf _vDisabledViaVrf;

  private Vrf _vEnabled;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _cEnabled = _cb.build();
    _cDisabled = _cb.build();
    _vb = _nf.vrfBuilder();
    _vEnabled = _vb.setOwner(_cEnabled).build();
    _vDisabledViaVrf = _vb.build();
    _vDisabledViaNode = _vb.setOwner(_cDisabled).build();
    _ib = _nf.interfaceBuilder().setOwner(_cEnabled).setActive(true);
    _iEnabled =
        _ib.setOwner(_cEnabled)
            .setVrf(_vEnabled)
            .setAddress(new InterfaceAddress("1.0.0.0/31"))
            .build();
    _iEnabledAndFlowSink = _ib.setAddress(new InterfaceAddress("2.0.0.0/31")).build();
    _iDisabledViaInactiveAndFlowSink =
        _ib.setActive(false).setAddress(new InterfaceAddress("4.0.0.0/31")).build();
    _iDisabledViaBlacklisted = _ib.setActive(true).setBlacklisted(true).setAddress(null).build();
    _iDisabledViaInterface = _ib.setBlacklisted(false).build();
    _iDisabledViaVrf = _ib.setVrf(_vDisabledViaVrf).build();
    // interface disabled via disabledNodes
    _ib.setOwner(_cDisabled).setVrf(_vDisabledViaNode).build();
    _inputBuilder =
        SynthesizerInput.builder()
            .setConfigurations(
                ImmutableMap.of(_cEnabled.getName(), _cEnabled, _cDisabled.getName(), _cDisabled))
            .setDisabledNodes(ImmutableSet.of(_cDisabled.getName()))
            .setDisabledVrfs(
                ImmutableMap.of(_cEnabled.getName(), ImmutableSet.of(_vDisabledViaVrf.getName())))
            .setDisabledInterfaces(
                ImmutableMap.of(
                    _cEnabled.getName(), ImmutableSet.of(_iDisabledViaInterface.getName())));
  }

  @Test
  public void testComputeEnabledFlowSinks() {
    DataPlane dp =
        TestDataPlane.builder()
            .setFlowSinks(
                ImmutableSet.of(
                    new NodeInterfacePair(_cEnabled.getName(), _iEnabledAndFlowSink.getName()),
                    new NodeInterfacePair(
                        _cEnabled.getName(), _iDisabledViaInactiveAndFlowSink.getName())))
            .build();
    SynthesizerInput input = _inputBuilder.setDataPlane(dp).build();

    assertThat(
        input,
        hasEnabledFlowSinks(
            equalTo(
                ImmutableSet.of(
                    new NodeInterfacePair(_cEnabled.getName(), _iEnabledAndFlowSink.getName())))));
  }

  @Test
  public void testComputeEnabledInterfaces() {
    SynthesizerInput input = _inputBuilder.build();

    assertThat(
        input,
        hasEnabledInterfaces(
            hasEntry(equalTo(_cEnabled.getName()), hasEntry(_iEnabled.getName(), _iEnabled))));
    assertThat(
        input,
        hasEnabledInterfaces(
            hasEntry(
                equalTo(_cEnabled.getName()),
                hasEntry(_iEnabledAndFlowSink.getName(), _iEnabledAndFlowSink))));
    assertThat(
        input,
        hasEnabledInterfaces(
            hasEntry(
                equalTo(_cEnabled.getName()),
                not(hasKey(_iDisabledViaInactiveAndFlowSink.getName())))));
    assertThat(
        input,
        hasEnabledInterfaces(
            hasEntry(
                equalTo(_cEnabled.getName()), not(hasKey(_iDisabledViaBlacklisted.getName())))));
    assertThat(
        input,
        hasEnabledInterfaces(
            hasEntry(equalTo(_cEnabled.getName()), not(hasKey(_iDisabledViaInterface.getName())))));
    assertThat(
        input,
        hasEnabledInterfaces(
            hasEntry(equalTo(_cEnabled.getName()), not(hasKey(_iDisabledViaVrf.getName())))));
    assertThat(input, hasEnabledInterfaces(not(hasKey(_cDisabled.getName()))));
  }

  @Test
  public void testComputeEnabledNodes() {
    SynthesizerInput input = _inputBuilder.build();

    assertThat(input, hasEnabledNodes(hasEntry(_cEnabled.getName(), _cEnabled)));
    assertThat(input, hasEnabledNodes(not(hasKey(_cDisabled.getName()))));
  }

  @Test
  public void testComputeEnabledVrfs() {
    SynthesizerInput input = _inputBuilder.build();

    assertThat(
        input,
        hasEnabledVrfs(
            hasEntry(equalTo(_cEnabled.getName()), hasEntry(_vEnabled.getName(), _vEnabled))));
    assertThat(
        input,
        hasEnabledVrfs(
            hasEntry(equalTo(_cEnabled.getName()), not(hasKey(_vDisabledViaVrf.getName())))));
    assertThat(input, hasEnabledVrfs(not(hasKey(_cDisabled.getName()))));
  }

  @Test
  public void testComputeFibConditions() {
    String nextHop1 = "nextHop1";
    String nextHopInterface1 = "nextHopInterface1";
    String nextHop2 = "nextHop2";
    String nextHopInterface2 = "nextHopInterface2";
    Prefix p1 = Prefix.parse("1.2.3.0/24");
    Prefix p2 = Prefix.parse("3.4.5.0/24");
    FibRow iEnabledFibRow11 = new FibRow(p1, _iEnabled.getName(), nextHop1, nextHopInterface1);
    FibRow iEnabledFibRow12 = new FibRow(p2, _iEnabled.getName(), nextHop1, nextHopInterface1);
    FibRow iEnabledFibRow21 = new FibRow(p1, _iEnabled.getName(), nextHop2, nextHopInterface2);
    FibRow iEnabledFibRow22 = new FibRow(p2, _iEnabled.getName(), nextHop2, nextHopInterface2);
    FibRow defaultDropFibRow = new FibRow(Prefix.ZERO, FibRow.DROP_NO_ROUTE, "", "");
    SortedSet<FibRow> fibs =
        ImmutableSortedSet.of(
            iEnabledFibRow11,
            iEnabledFibRow12,
            iEnabledFibRow21,
            iEnabledFibRow22,
            defaultDropFibRow);
    DataPlane dp =
        TestDataPlane.builder()
            .setFibs(
                ImmutableMap.of(_cEnabled.getName(), ImmutableMap.of(_vEnabled.getName(), fibs)))
            .build();
    SynthesizerInput input = _inputBuilder.setDataPlane(dp).build();

    assertThat(
        input,
        SynthesizerInputMatchers.hasFibConditions(
            equalTo(
                ImmutableMap.of(
                    _cEnabled.getName(),
                    ImmutableMap.of(
                        _vEnabled.getName(),
                        ImmutableMap.of(
                            _iEnabled.getName(),
                            ImmutableMap.of(
                                new NodeInterfacePair(nextHop1, nextHopInterface1),
                                new OrExpr(
                                    ImmutableList.of(
                                        FibRowMatchExpr.getFibRowConditions(
                                            _cEnabled.getName(),
                                            _vEnabled.getName(),
                                            ImmutableList.copyOf(fibs),
                                            1,
                                            iEnabledFibRow11),
                                        FibRowMatchExpr.getFibRowConditions(
                                            _cEnabled.getName(),
                                            _vEnabled.getName(),
                                            ImmutableList.copyOf(fibs),
                                            3,
                                            iEnabledFibRow12))),
                                new NodeInterfacePair(nextHop2, nextHopInterface2),
                                new OrExpr(
                                    ImmutableList.of(
                                        FibRowMatchExpr.getFibRowConditions(
                                            _cEnabled.getName(),
                                            _vEnabled.getName(),
                                            ImmutableList.copyOf(fibs),
                                            2,
                                            iEnabledFibRow21),
                                        FibRowMatchExpr.getFibRowConditions(
                                            _cEnabled.getName(),
                                            _vEnabled.getName(),
                                            ImmutableList.copyOf(fibs),
                                            4,
                                            iEnabledFibRow22)))),
                            FibRow.DROP_NO_ROUTE,
                            ImmutableMap.of(
                                new NodeInterfacePair("", ""),
                                new OrExpr(
                                    ImmutableList.of(
                                        FibRowMatchExpr.getFibRowConditions(
                                            _cEnabled.getName(),
                                            _vEnabled.getName(),
                                            ImmutableList.copyOf(fibs),
                                            0,
                                            defaultDropFibRow))))))))));
  }

  @Test
  public void testComputeIpsByHostname() {
    SynthesizerInput input = _inputBuilder.setDataPlane(TestDataPlane.builder().build()).build();

    assertThat(
        input,
        hasIpsByHostname(
            equalTo(
                ImmutableMap.of(
                    _cEnabled.getName(),
                    ImmutableSet.of(
                        _iEnabled.getAddress().getIp(),
                        _iEnabledAndFlowSink.getAddress().getIp())))));
  }
}
