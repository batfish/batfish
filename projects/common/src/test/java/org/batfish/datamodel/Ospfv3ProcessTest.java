package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3Process;
import org.junit.Test;

/** Tests for {@link Ospfv3Process}. */
public final class Ospfv3ProcessTest {

  @Test
  public void testDefaults() {
    Ospfv3Process process =
        Ospfv3Process.builder()
            .setProcessId("1")
            .setRouterId(Ip.parse("192.0.2.1"))
            .build();

    assertThat(
        process.getAdminCost(),
        equalTo(Ospfv3Process.DEFAULT_ADMIN_COST));
    assertThat(
        process.getReferenceBandwidth(),
        equalTo(
            Ospfv3Process.DEFAULT_REFERENCE_BANDWIDTH));
    assertThat(
        process.getRedistributeConnected(),
        equalTo(false));
    assertThat(
        process.getRedistributionMetric(),
        equalTo(
            Ospfv3Process.DEFAULT_REDISTRIBUTION_METRIC));
  }

  @Test
  public void testSerialization() {
    Ospfv3Area area =
        Ospfv3Area.builder()
            .setNumber(0L)
            .addInterface("Ethernet1")
            .setStub(true)
            .setSuppressInterArea(true)
            .setDefaultMetric(17L)
            .build();

    Ospfv3Process process =
        Ospfv3Process.builder()
            .setProcessId("1")
            .setRouterId(Ip.parse("192.0.2.1"))
            .setAreas(ImmutableMap.of(0L, area))
            .setAdminCost(111)
            .setReferenceBandwidth(40_000_000_000D)
            .setRedistributeConnected(true)
            .setRedistributionMetric(37L)
            .build();

    Ospfv3Process clone =
        BatfishObjectMapper.clone(
            process, Ospfv3Process.class);

    assertThat(clone.getProcessId(), equalTo("1"));
    assertThat(
        clone.getRouterId(),
        equalTo(Ip.parse("192.0.2.1")));
    assertThat(
        clone.getAreas().keySet(),
        equalTo(process.getAreas().keySet()));
    assertThat(
        clone.getAreas().get(0L).getStub(),
        equalTo(true));
    assertThat(
        clone.getAreas()
            .get(0L)
            .getSuppressInterArea(),
        equalTo(true));
    assertThat(
        clone.getAreas()
            .get(0L)
            .getDefaultMetric(),
        equalTo(17L));
    assertThat(clone.getAdminCost(), equalTo(111));
    assertThat(
        clone.getReferenceBandwidth(),
        equalTo(40_000_000_000D));
    assertThat(
        clone.getRedistributeConnected(),
        equalTo(true));
    assertThat(
        clone.getRedistributionMetric(),
        equalTo(37L));
  }
}
