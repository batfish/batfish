package org.batfish.engine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;
import org.batfish.engine.SnapshotRequest.Device;
import org.batfish.engine.SnapshotRequest.Environment;
import org.batfish.engine.SnapshotRequest.Link;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class OverlayTest {
  @Rule public TemporaryFolder folder = new TemporaryFolder();

  static String config(int host, boolean flood) {
    int peer = 3 - host;
    return """
    !RANCID-CONTENT-TYPE: arista
    hostname leaf%d
    vlan 110
    vrf instance TENANT
    interface Ethernet1
      no switchport
      ip address 192.0.2.%d/30
    interface Ethernet2
      switchport access vlan 110
    interface Loopback0
      ip address 203.0.113.%d/32
    interface Vlan110
      vrf TENANT
      ip address 10.110.0.%d/24
    interface Vxlan1
      vxlan source-interface Loopback0
      vxlan vlan 110 vni 10110
      vxlan vrf TENANT vni 50110
    %s
    ip routing
    ip routing vrf TENANT
    ip route 203.0.113.%d/32 192.0.2.%d
    end
    """
        .formatted(
            host,
            host,
            host,
            host,
            flood ? "  vxlan flood vtep 203.0.113." + peer : "",
            peer,
            peer);
  }

  static SnapshotRequest request(boolean flood) {
    return new SnapshotRequest(
        List.of(
            new Device("leaf1", Dialect.ARISTA_EOS, config(1, flood)),
            new Device("leaf2", Dialect.ARISTA_EOS, config(2, flood))),
        List.of(new Link("leaf1", "Ethernet1", "leaf2", "Ethernet1")),
        Environment.NONE);
  }

  @Test
  public void exportsL2L3VnisAndComputesTunnelsWithoutInferringFromVniNumbers() throws Exception {
    try (EngineService service =
        new EngineService(new EngineBatfish(folder.newFolder().toPath(), 4))) {
      var job = service.put("overlay", request(true));
      awaitJob(service, job.jobId());
      var facts = service.facts("overlay");
      var vnis = facts.devices().get("leaf1").vnis();
      assertThat(vnis, hasSize(2));
      var l2 = vnis.stream().filter(v -> v.layer().equals("l2")).findFirst().orElseThrow();
      assertThat(l2.vni(), equalTo(10110));
      assertThat(l2.vlan(), equalTo(110));
      assertThat(l2.sourceAddress(), equalTo("203.0.113.1"));
      assertThat(l2.bumTransportIps(), equalTo(List.of("203.0.113.2")));
      assertThat(
          vnis.stream().filter(v -> v.layer().equals("l3")).findFirst().orElseThrow().vrf(),
          equalTo("TENANT"));
      var answer =
          org.batfish.common.util.BatfishObjectMapper.mapper()
              .valueToTree(service.ask("overlay", "vxlanEdges", null).answer());
      assertThat(answer.path("rows").size(), equalTo(2));
      var disconnected = service.put("no-flood", request(false));
      awaitJob(service, disconnected.jobId());
      var absent =
          org.batfish.common.util.BatfishObjectMapper.mapper()
              .valueToTree(service.ask("no-flood", "vxlanEdges", null).answer());
      assertThat(absent.path("rows").size(), equalTo(0));
    }
  }

  static String evpnConfig(int host, boolean matchingRt, boolean reachable) {
    int peer = 3 - host;
    String bgp =
        """
        interface Loopback110
          vrf TENANT
          ip address 10.111.%d.1/32
        router bgp 65110
          router-id 203.0.113.%d
          no bgp default ipv4-unicast
          neighbor 192.0.2.%d remote-as 65110
          neighbor 192.0.2.%d send-community extended
          address-family evpn
            neighbor 192.0.2.%d activate
          vrf TENANT
            rd 203.0.113.%d:50110
            route-target import evpn %d:50110
            route-target export evpn %d:50110
            redistribute connected
        """
            .formatted(
                host,
                host,
                peer,
                peer,
                peer,
                host,
                matchingRt || host == 1 ? 65110 : 65111,
                matchingRt || host == 1 ? 65110 : 65111);
    String base = config(host, true).replace("end\n", bgp + "end\n");
    return reachable
        ? base
        : base.replace("ip route 203.0.113." + peer + "/32 192.0.2." + peer, "");
  }

  static SnapshotRequest evpnRequest(boolean matchingRt, boolean reachable) {
    return new SnapshotRequest(
        List.of(
            new Device("leaf1", Dialect.ARISTA_EOS, evpnConfig(1, matchingRt, reachable)),
            new Device("leaf2", Dialect.ARISTA_EOS, evpnConfig(2, matchingRt, reachable))),
        List.of(new Link("leaf1", "Ethernet1", "leaf2", "Ethernet1")),
        Environment.NONE);
  }

  @Test
  public void l3TunnelsRequireImportedEvpnRoutesAndReachableUnderlay() throws Exception {
    try (EngineService service =
        new EngineService(new EngineBatfish(folder.newFolder().toPath(), 4))) {
      for (String id : List.of("evpn", "rt-mismatch", "unreachable")) {
        var job =
            service.put(id, evpnRequest(!id.equals("rt-mismatch"), !id.equals("unreachable")));
        awaitJob(service, job.jobId());
        var mapper = org.batfish.common.util.BatfishObjectMapper.mapper();
        var rows = mapper.valueToTree(service.ask(id, "vxlanEdges", null).answer()).path("rows");
        long l3 =
            java.util.stream.StreamSupport.stream(rows.spliterator(), false)
                .filter(row -> row.path("VNI_Layer").asText().equals("l3"))
                .count();
        assertThat(id, l3, equalTo(id.equals("evpn") ? 2L : 0L));
        var properties =
            mapper.valueToTree(service.ask(id, "evpnL3VniProperties", null).answer()).path("rows");
        assertThat(properties.size(), equalTo(2));
        for (var row : properties) {
          assertThat(row.path("VNI").asInt(), equalTo(50110));
          assertThat(row.path("VRF").asText(), equalTo("TENANT"));
          assertThat(row.path("Import_Route_Targets").isEmpty(), equalTo(false));
          assertThat(
              row.path("Export_Route_Targets").get(0).asText(),
              equalTo(
                  id.equals("rt-mismatch") && row.path("Node").asText().equals("leaf2")
                      ? "65111:50110"
                      : "65110:50110"));
        }
        if (id.equals("evpn")) {
          var routes = mapper.valueToTree(service.ask(id, "evpnRib", null).answer()).path("rows");
          assertThat(routes.isEmpty(), equalTo(false));
        }
      }
    }
  }

  private static void awaitJob(EngineService service, String id) throws Exception {
    for (int attempt = 0; attempt < 600; attempt++) {
      String status = service.job(id).status().name();
      if (status.equals("done")) return;
      if (status.equals("failed")) throw new AssertionError(service.job(id).error());
      Thread.sleep(50);
    }
    throw new AssertionError("overlay build timed out");
  }
}
