package org.batfish.common.runtime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link SnapshotRuntimeData} */
public class SnapshotRuntimeDataTest {
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testJsonSerialization() {
    SnapshotRuntimeData.Builder builder =
        SnapshotRuntimeData.builder()
            .setRuntimeData(ImmutableMap.of("hostname", RuntimeData.EMPTY_RUNTIME_DATA));
    assertThat(
        BatfishObjectMapper.clone(builder.build(), SnapshotRuntimeData.class),
        equalTo(builder.build()));
  }

  @Test
  public void testProcessedCorrectlyOnInitialization() {
    Map<String, RuntimeData> initData = new TreeMap<>();
    initData.put("HOSTNAME", RuntimeData.EMPTY_RUNTIME_DATA);

    // Can't use builder because setRuntimeData throws on non-canonical hostnames
    SnapshotRuntimeData snapshotRuntimeData = new SnapshotRuntimeData(initData);

    // Processed data should contain only the lower-cased hostname, and should be immutable
    assertThat(
        snapshotRuntimeData.getRuntimeData(),
        equalTo(ImmutableMap.of("hostname", RuntimeData.EMPTY_RUNTIME_DATA)));
    assertTrue(snapshotRuntimeData.getRuntimeData() instanceof ImmutableMap);
  }

  @Test
  public void testGetRuntimeDataForNode() {
    String hostname = "hostname";
    RuntimeData runtimeData =
        RuntimeData.builder()
            .setInterfaces(
                ImmutableMap.of("iface", InterfaceRuntimeData.EMPTY_INTERFACE_RUNTIME_DATA))
            .build();
    SnapshotRuntimeData srd =
        SnapshotRuntimeData.builder()
            .setRuntimeData(ImmutableMap.of(hostname, runtimeData))
            .build();

    // Existing hostname: should find data
    assertThat(srd.getRuntimeData(hostname), equalTo(runtimeData));

    // Non-existent hostname: should return empty runtime data
    assertThat(srd.getRuntimeData("other"), equalTo(RuntimeData.EMPTY_RUNTIME_DATA));

    // Non-canonical hostname: should throw
    _thrown.expect(AssertionError.class);
    srd.getRuntimeData(hostname.toUpperCase());
  }

  @Test
  public void testBuilderSetRuntimeDataThrowsOnNonCanonicalHostname() {
    String hostname = "HOSTNAME";
    _thrown.expect(IllegalArgumentException.class);
    SnapshotRuntimeData.builder()
        .setRuntimeData(ImmutableMap.of(hostname, RuntimeData.EMPTY_RUNTIME_DATA));
  }

  @Test
  public void testBuilderSetInterfacesLineUp() {
    String n1 = "n1";
    String n2 = "n2";
    String i1 = "i1";
    String i2 = "i2";
    InterfaceRuntimeData lineUp = InterfaceRuntimeData.builder().setLineUp(true).build();
    SnapshotRuntimeData.Builder builder = SnapshotRuntimeData.builder();

    // Setting an existing interface should affect only that interface
    {
      RuntimeData runtimeData1 =
          RuntimeData.builder().setInterfaces(ImmutableMap.of(i1, lineUp, i2, lineUp)).build();
      RuntimeData runtimeData2 =
          RuntimeData.builder().setInterfaces(ImmutableMap.of(i1, lineUp)).build();

      builder.setRuntimeData(ImmutableMap.of(n1, runtimeData1, n2, runtimeData2));

      assertThat(
          builder
              .setInterfacesLineUp(ImmutableSet.of(NodeInterfacePair.of(n1, i1)), false)
              .build()
              .getRuntimeData(),
          equalTo(
              ImmutableMap.of(
                  n1,
                  runtimeData1.toBuilder().setInterfaceLineUp(i1, false).build(),
                  n2,
                  runtimeData2)));
    }

    // Setting interfaces on a missing node should add runtime data for the node
    {
      builder.setRuntimeData(ImmutableMap.of(n1, RuntimeData.EMPTY_RUNTIME_DATA));
      Set<NodeInterfacePair> ifaces =
          ImmutableSet.of(NodeInterfacePair.of(n1, i1), NodeInterfacePair.of(n2, i1));
      RuntimeData i1UpRuntimeData = RuntimeData.builder().setInterfaceLineUp(i1, true).build();
      assertThat(
          builder.setInterfacesLineUp(ifaces, true).build().getRuntimeData(),
          equalTo(ImmutableMap.of(n1, i1UpRuntimeData, n2, i1UpRuntimeData)));
    }
  }
}
