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
import org.batfish.common.topology.RuntimeData.InterfaceRuntimeData;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link RuntimeData} */
public class RuntimeDataTest {

  @Test
  public void testInterfaceRuntimeDataEquals() {
    double bandwidth = 1;
    double speed = 2;
    InterfaceRuntimeData ird = new InterfaceRuntimeData(bandwidth, speed);
    InterfaceRuntimeData irdNullBandwidth = new InterfaceRuntimeData(null, speed);
    InterfaceRuntimeData irdNullSpeed = new InterfaceRuntimeData(bandwidth, null);
    InterfaceRuntimeData irdNullBoth = new InterfaceRuntimeData(null, null);
    new EqualsTester()
        .addEqualityGroup(ird, new InterfaceRuntimeData(bandwidth, speed))
        .addEqualityGroup(new InterfaceRuntimeData(bandwidth + 1, speed))
        .addEqualityGroup(new InterfaceRuntimeData(bandwidth, speed + 1))
        .addEqualityGroup(irdNullBandwidth, new InterfaceRuntimeData(null, speed))
        .addEqualityGroup(irdNullSpeed, new InterfaceRuntimeData(bandwidth, null))
        .addEqualityGroup(irdNullBoth, new InterfaceRuntimeData(null, null))
        .testEquals();
  }

  @Test
  public void testInterfaceRuntimeDataJsonSerialization() throws IOException {
    InterfaceRuntimeData ird = new InterfaceRuntimeData(1d, 2d);
    assertThat(BatfishObjectMapper.clone(ird, InterfaceRuntimeData.class), equalTo(ird));
  }

  @Test
  public void testRuntimeDataJsonSerialization() throws IOException {
    RuntimeData rd =
        new RuntimeData(
            ImmutableMap.of("c", ImmutableMap.of("i", new InterfaceRuntimeData(1d, 2d))));
    assertThat(BatfishObjectMapper.clone(rd, RuntimeData.class), equalTo(rd));
  }

  @Test
  public void testDataProcessedCorrectlyOnInitialization() {
    String hostname = "NODE";
    String iface = "IFACE";
    Map<String, InterfaceRuntimeData> ifaceInitData = new TreeMap<>();
    ifaceInitData.put(iface, new InterfaceRuntimeData(1d, 2d));
    Map<String, Map<String, InterfaceRuntimeData>> initData = new TreeMap<>();
    initData.put(hostname, ifaceInitData);

    Map<String, Map<String, InterfaceRuntimeData>> runtimeData =
        new RuntimeData(initData).getInterfaceRuntimeData();

    // Processed data should contain only the lower-cased hostname, and should be immutable
    assertThat(runtimeData.keySet(), contains(hostname.toLowerCase()));
    assertTrue(runtimeData instanceof ImmutableMap);

    // Processed interface data should contain the original info, but should be immutable
    Map<String, InterfaceRuntimeData> ifaceRuntimeData = runtimeData.get(hostname.toLowerCase());
    assertThat(ifaceRuntimeData, equalTo(ifaceInitData));
    assertTrue(ifaceRuntimeData instanceof ImmutableMap);
  }
}
