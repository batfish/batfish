package org.batfish.common.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.TreeMap;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link RuntimeData} */
public class RuntimeDataTest {

  @Test
  public void testJsonSerialization() {
    RuntimeData.Builder builder =
        RuntimeData.builder()
            .setInterfaces(ImmutableMap.of("iface", InterfaceRuntimeData.builder().build()));
    assertThat(
        BatfishObjectMapper.clone(builder.build(), RuntimeData.class), equalTo(builder.build()));
  }

  @Test
  public void testProcessedCorrectlyOnInitialization() {
    Map<String, InterfaceRuntimeData> ifaceInitData = new TreeMap<>();
    ifaceInitData.put("iface", InterfaceRuntimeData.builder().build());

    RuntimeData runtimeData = RuntimeData.builder().setInterfaces(ifaceInitData).build();

    // Processed data should be unchanged but immutable
    assertThat(runtimeData.getInterfaces(), equalTo(ifaceInitData));
    assertTrue(runtimeData.getInterfaces() instanceof ImmutableMap);
  }

  @Test
  public void testGetInterface() {
    String iface = "iface";
    InterfaceRuntimeData ifaceData = InterfaceRuntimeData.EMPTY_INTERFACE_RUNTIME_DATA;
    RuntimeData data =
        RuntimeData.builder().setInterfaces(ImmutableMap.of(iface, ifaceData)).build();

    // Existing interface: should find data
    assertThat(data.getInterface(iface), equalTo(ifaceData));

    // Non-existent interface: should return null
    assertNull(data.getInterface("other"));
  }

  @Test
  public void testBuilderSetInterfaceLineUp() {
    String i1 = "i1";
    String i2 = "i2";
    InterfaceRuntimeData lineUp = InterfaceRuntimeData.builder().setLineUp(true).build();
    InterfaceRuntimeData lineDown = InterfaceRuntimeData.builder().setLineUp(false).build();
    RuntimeData.Builder builder = RuntimeData.builder();

    // Setting an existing interface should affect only that interface
    {
      Map<String, InterfaceRuntimeData> initialIfaceData =
          ImmutableMap.of(i1, lineUp, i2, lineDown);
      builder.setInterfaces(initialIfaceData);
      assertThat(
          builder.setInterfaceLineUp(i1, true).build().getInterfaces(), equalTo(initialIfaceData));

      Map<String, InterfaceRuntimeData> changedIfaceData =
          ImmutableMap.of(i1, lineDown, i2, lineDown);
      assertThat(
          builder.setInterfaceLineUp(i1, false).build().getInterfaces(), equalTo(changedIfaceData));
    }

    // Setting an existing interface should not affect other properties of that interface
    {
      InterfaceRuntimeData ird = lineUp.toBuilder().setBandwidth(1d).setSpeed(2d).build();
      Map<String, InterfaceRuntimeData> initialIfaceData = ImmutableMap.of(i1, ird);
      builder.setInterfaces(initialIfaceData);
      assertThat(
          builder.setInterfaceLineUp(i1, false).build().getInterface(i1),
          equalTo(ird.toBuilder().setLineUp(false).build()));
    }

    // Setting a nonexistent interface should add it
    {
      builder.setInterfaces(ImmutableMap.of(i1, lineUp));
      assertThat(
          builder.setInterfaceLineUp(i2, false).build().getInterfaces(),
          equalTo(ImmutableMap.of(i1, lineUp, i2, lineDown)));
    }
  }
}
