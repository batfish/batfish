package org.batfish.common.topology;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import org.batfish.common.topology.SnapshotRuntimeData.InterfaceRuntimeData;
import org.batfish.common.topology.SnapshotRuntimeData.RuntimeData;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link SnapshotRuntimeData} */
public class SnapshotRuntimeDataTest {

  private static final String NODE = "NODE";
  private static final String IFACE = "IFACE";
  private static final double BANDWIDTH = 1;
  private static final double SPEED = 2;

  private static final InterfaceRuntimeData INTERFACE_RUNTIME_DATA =
      new InterfaceRuntimeData(BANDWIDTH, SPEED);
  private static final RuntimeData RUNTIME_DATA =
      new RuntimeData(ImmutableMap.of(IFACE, INTERFACE_RUNTIME_DATA));

  @Test
  public void testInterfaceRuntimeDataEquals() {
    InterfaceRuntimeData irdNullBandwidth = new InterfaceRuntimeData(null, SPEED);
    InterfaceRuntimeData irdNullSpeed = new InterfaceRuntimeData(BANDWIDTH, null);
    InterfaceRuntimeData irdNullBoth = new InterfaceRuntimeData(null, null);
    new EqualsTester()
        .addEqualityGroup(INTERFACE_RUNTIME_DATA, new InterfaceRuntimeData(BANDWIDTH, SPEED))
        .addEqualityGroup(new InterfaceRuntimeData(BANDWIDTH + 1, SPEED))
        .addEqualityGroup(new InterfaceRuntimeData(BANDWIDTH, SPEED + 1))
        .addEqualityGroup(irdNullBandwidth, new InterfaceRuntimeData(null, SPEED))
        .addEqualityGroup(irdNullSpeed, new InterfaceRuntimeData(BANDWIDTH, null))
        .addEqualityGroup(irdNullBoth, new InterfaceRuntimeData(null, null))
        .testEquals();
  }

  @Test
  public void testInterfaceRuntimeDataJsonSerialization() throws IOException {
    assertThat(
        BatfishObjectMapper.clone(INTERFACE_RUNTIME_DATA, InterfaceRuntimeData.class),
        equalTo(INTERFACE_RUNTIME_DATA));
  }

  @Test
  public void testRuntimeDataJsonSerialization() throws IOException {
    assertThat(BatfishObjectMapper.clone(RUNTIME_DATA, RuntimeData.class), equalTo(RUNTIME_DATA));
  }

  @Test
  public void testSnapshotRuntimeDataJsonSerialization() throws IOException {
    SnapshotRuntimeData srd = new SnapshotRuntimeData(ImmutableMap.of(NODE, RUNTIME_DATA));
    assertThat(BatfishObjectMapper.clone(srd, SnapshotRuntimeData.class), equalTo(srd));
  }

  @Test
  public void testRuntimeDataProcessedCorrectlyOnInitialization() {
    Map<String, InterfaceRuntimeData> ifaceInitData = new TreeMap<>();
    ifaceInitData.put(IFACE, INTERFACE_RUNTIME_DATA);

    Map<String, InterfaceRuntimeData> ifaceRuntimeData =
        new RuntimeData(ifaceInitData).getInterfaces();

    // Processed data should be unchanged but immutable
    assertThat(ifaceRuntimeData, equalTo(ifaceInitData));
    assertTrue(ifaceRuntimeData instanceof ImmutableMap);
  }

  @Test
  public void testSnapshotRuntimeDataProcessedCorrectlyOnInitialization() {
    Map<String, RuntimeData> initData = new TreeMap<>();
    initData.put(NODE, RUNTIME_DATA);

    Map<String, RuntimeData> snapshotRuntimeData =
        new SnapshotRuntimeData(initData).getRuntimeData();

    // Processed data should contain only the lower-cased hostname, and should be immutable
    assertThat(snapshotRuntimeData.keySet(), contains(NODE.toLowerCase()));
    assertThat(snapshotRuntimeData.values(), contains(RUNTIME_DATA));
    assertTrue(snapshotRuntimeData instanceof ImmutableMap);
  }
}
