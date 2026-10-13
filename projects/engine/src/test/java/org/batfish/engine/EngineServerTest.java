package org.batfish.engine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.hash.Hashing;
import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.engine.Deriver.Change;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** The HTTP API, end to end, with every request and response checked against openapi.yaml. */
public final class EngineServerTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static final OpenApiContract CONTRACT = OpenApiContract.load();

  private EngineService _service;
  private EngineServer _server;
  private HttpClient _http;

  private record Response(int status, JsonNode body) {}

  @Before
  public void setUp() throws Exception {
    _service = new EngineService(new EngineBatfish(_folder.newFolder().toPath(), 4));
    _server = new EngineServer(_service, new InetSocketAddress("127.0.0.1", 0));
    _server.start();
    _http = HttpClient.newHttpClient();
  }

  @After
  public void tearDown() {
    _server.close();
    _service.close();
  }

  /** Calls the engine, checking the request and the response against the contract. */
  private Response call(String method, String template, String path, Object body) throws Exception {
    JsonNode request = body == null ? null : BatfishObjectMapper.mapper().valueToTree(body);
    if (request != null) {
      assertThat(
          method + " " + template + " request",
          CONTRACT.checkRequest(template, method, request),
          empty());
    }
    HttpRequest.BodyPublisher publisher =
        request == null
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(
                BatfishObjectMapper.mapper().writeValueAsString(request));
    HttpResponse<String> res =
        _http.send(
            HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + _server.port() + path))
                .method(method, publisher)
                .header("Content-Type", "application/json")
                .build(),
            HttpResponse.BodyHandlers.ofString());
    JsonNode json = res.body().isEmpty() ? null : BatfishObjectMapper.mapper().readTree(res.body());
    List<String> errors = CONTRACT.checkResponse(template, method, res.statusCode(), json);
    assertThat(method + " " + path + " -> " + res.body(), errors, empty());
    return new Response(res.statusCode(), json);
  }

  private void awaitJob(String jobId) throws Exception {
    for (int i = 0; i < 600; i++) {
      Response job = call("GET", "/jobs/{id}", "/jobs/" + jobId, null);
      String status = job.body().get("status").asText();
      if (status.equals("done")) {
        return;
      }
      assertThat("job failed: " + job.body(), status, not(equalTo("failed")));
      Thread.sleep(50);
    }
    throw new AssertionError("job " + jobId + " did not finish");
  }

  private void putAndAwait(String id, SnapshotRequest request) throws Exception {
    Response put = call("PUT", "/snapshots/{id}", "/snapshots/" + id, request);
    assertThat(put.body().toString(), put.status(), equalTo(202));
    awaitJob(put.body().get("jobId").asText());
  }

  private static List<String> column(JsonNode answer, String column) {
    List<String> values = new ArrayList<>();
    answer.path("answer").path("rows").forEach(row -> values.add(row.path(column).toString()));
    return values;
  }

  @Test
  public void health() throws Exception {
    Response health = call("GET", "/health", "/health", null);
    assertThat(health.status(), equalTo(200));
  }

  @Test
  public void exactPacketAclEvidenceMatchesIngressPathAndDistinguishesImplicitDeny()
      throws Exception {
    putAndAwait(
        "packet-acl",
        new SnapshotRequest(
            List.of(
                new SnapshotRequest.Device(
                    "edge",
                    Dialect.ARISTA_EOS,
                    """
                    !RANCID-CONTENT-TYPE: arista
                    hostname edge
                    ip routing
                    ip access-list WEB
                       10 permit tcp any host 192.0.2.1 eq 443
                       20 deny tcp any host 192.0.2.1 eq 22
                    interface Ethernet1
                       no switchport
                       ip address 192.0.2.1/24
                       ip access-group WEB in
                    end
                    """)),
            List.of(),
            SnapshotRequest.Environment.NONE));
    for (int port : List.of(443, 22, 80)) {
      var mapper = BatfishObjectMapper.mapper();
      ObjectNode headers =
          (ObjectNode)
              mapper.readTree(
                  """
                  {
                    "srcIps":"192.0.2.10", "dstIps":"192.0.2.1",
                    "srcPorts":"49152", "dstPorts":"%d", "ipProtocols":["TCP"],
                    "dscps":"8", "ecns":"0", "packetLengths":"64", "fragmentOffsets":"0",
                    "tcpFlags":[{
                      "useAck":true, "useCwr":true, "useEce":true, "useFin":true,
                      "usePsh":true, "useRst":true, "useSyn":true, "useUrg":true,
                      "tcpFlags":{"syn":true}
                    }]
                  }
                  """
                      .formatted(port));
      String location = "@enter(\"edge\"[\"Ethernet1\"])";
      Response filters =
          call(
              "POST",
              "/snapshots/{id}/questions/{question}",
              "/snapshots/packet-acl/questions/testFilters",
              Map.of(
                  "nodes",
                  "\"edge\"",
                  "filters",
                  "\"WEB\"",
                  "startLocation",
                  location,
                  "headers",
                  headers));
      assertThat(filters.body().toString(), filters.status(), equalTo(200));
      JsonNode rows = filters.body().path("answer").path("rows");
      assertThat(rows.size(), equalTo(1));
      JsonNode row = rows.get(0);
      assertThat(row.toString(), row.path("Node").path("name").asText(), equalTo("edge"));
      assertThat(row.path("Filter_Name").asText(), equalTo("WEB"));
      assertThat(row.path("Action").asText(), equalTo(port == 443 ? "PERMIT" : "DENY"));
      if (port == 80) {
        assertThat(row.path("Line_Content").asText(), equalTo("no-match"));
      } else {
        assertThat(
            row.path("Line_Content").asText(),
            containsString(port == 443 ? "permit tcp" : "deny tcp"));
        assertThat(row.path("Trace").isArray(), equalTo(true));
        assertThat(row.path("Trace").isEmpty(), equalTo(false));
      }
      JsonNode flow = row.path("Flow");
      assertThat(flow.path("ingressNode").asText(), equalTo("edge"));
      assertThat(flow.path("ingressInterface").asText(), equalTo("Ethernet1"));
      assertThat(flow.path("srcIp").asText(), equalTo("192.0.2.10"));
      assertThat(flow.path("dstIp").asText(), equalTo("192.0.2.1"));
      assertThat(flow.path("srcPort").asInt(), equalTo(49152));
      assertThat(flow.path("dstPort").asInt(), equalTo(port));
      assertThat(flow.path("ipProtocol").asText(), equalTo("TCP"));
      assertThat(flow.path("dscp").asInt(), equalTo(8));
      assertThat(flow.path("ecn").asInt(), equalTo(0));
      assertThat(flow.path("packetLength").asInt(), equalTo(64));
      assertThat(flow.path("fragmentOffset").asInt(), equalTo(0));
      assertThat(flow.path("tcpFlagsSyn").asInt(), equalTo(1));
      assertThat(flow.path("tcpFlagsAck").asInt(), equalTo(0));
      Response path =
          call(
              "POST",
              "/snapshots/{id}/questions/{question}",
              "/snapshots/packet-acl/questions/traceroute",
              Map.of("startLocation", location, "headers", headers, "ignoreFilters", false));
      assertThat(path.body().toString(), path.status(), equalTo(200));
      JsonNode trace = path.body().path("answer").path("rows").get(0).path("Traces").get(0);
      assertThat(
          trace.path("disposition").asText(), equalTo(port == 443 ? "ACCEPTED" : "DENIED_IN"));
      List<JsonNode> steps = new ArrayList<>();
      trace.path("hops").forEach(h -> h.path("steps").forEach(steps::add));
      JsonNode filter =
          steps.stream()
              .filter(s -> s.path("type").asText().equals("Filter"))
              .findFirst()
              .orElseThrow();
      assertThat(filter.path("action").asText(), equalTo(port == 443 ? "PERMITTED" : "DENIED"));
      assertThat(filter.path("detail").path("filter").asText(), equalTo("WEB"));
      assertThat(filter.path("detail").path("type").asText(), equalTo("INGRESS_FILTER"));
      assertThat(filter.path("detail").path("inputInterface").asText(), equalTo("Ethernet1"));
      assertThat(filter.path("detail").path("flow"), equalTo(flow));
    }
  }

  @Test
  public void buildFactsAndQuestions() throws Exception {
    putAndAwait("rev-1", Fixtures.frrOspfBgp());

    Response snapshot = call("GET", "/snapshots/{id}", "/snapshots/rev-1", null);
    assertThat(snapshot.body().get("status").asText(), equalTo("done"));
    Response facts = call("GET", "/snapshots/{id}/facts", "/snapshots/rev-1/facts", null);
    assertThat(
        facts.body().path("devices").path("r1").path("policies").get(0).path("name").asText(),
        equalTo("EXPORT-SITE"));

    Response routes =
        call(
            "POST",
            "/snapshots/{id}/questions/{question}",
            "/snapshots/rev-1/questions/routes",
            Map.of("nodes", "r2", "network", "10.1.0.0/24"));
    assertThat(routes.status(), equalTo(200));
    assertThat(column(routes.body(), "Protocol"), hasItem("\"bgp\""));

    Response sessions =
        call(
            "POST",
            "/snapshots/{id}/questions/{question}",
            "/snapshots/rev-1/questions/bgpSessionStatus",
            null);
    assertThat(column(sessions.body(), "Established_Status"), hasItem("\"ESTABLISHED\""));

    Response trace =
        call(
            "POST",
            "/snapshots/{id}/questions/{question}",
            "/snapshots/rev-1/questions/traceroute",
            Map.of("startLocation", "r1", "headers", Map.of("dstIps", "10.2.0.10")));
    assertThat(trace.status(), equalTo(200));
    assertThat(trace.body().toString(), containsString("DELIVERED_TO_SUBNET"));
  }

  @Test
  public void putIsIdempotentAndConflictsOnDifferentContent() throws Exception {
    Response first = call("PUT", "/snapshots/{id}", "/snapshots/rev-1", Fixtures.crpd());
    Response again = call("PUT", "/snapshots/{id}", "/snapshots/rev-1", Fixtures.crpd());
    assertThat(again.body().get("jobId"), equalTo(first.body().get("jobId")));
    awaitJob(first.body().get("jobId").asText());

    Response conflict = call("PUT", "/snapshots/{id}", "/snapshots/rev-1", Fixtures.dc1Eos());
    assertThat(conflict.status(), equalTo(409));
  }

  @Test
  public void deriveAndDiff() throws Exception {
    putAndAwait("live", Fixtures.frrOspfBgp());

    Response derive =
        call(
            "POST",
            "/snapshots/{id}/derive",
            "/snapshots/live/derive",
            new EngineServer.DeriveRequest(
                "change-1", List.of(new Change("r1", List.of("route-map EXPORT-SITE deny 5")))));
    assertThat(derive.status(), equalTo(202));
    awaitJob(derive.body().get("jobId").asText());

    Response proposed = call("GET", "/snapshots/{id}", "/snapshots/change-1", null);
    assertThat(proposed.body().get("base").asText(), equalTo("live"));
    assertThat(proposed.body().get("introduced").size(), equalTo(0));

    ObjectNode params = BatfishObjectMapper.mapper().createObjectNode();
    params.put("nodes", "r2");
    Response diff =
        call(
            "POST",
            "/diff",
            "/diff",
            new EngineServer.DiffRequest("live", "change-1", "routes", params));
    assertThat(diff.status(), equalTo(200));
    assertThat(diff.body().toString(), containsString("10.1.0.0/24"));
    assertThat(diff.body().toString(), containsString("Only in Reference"));
  }

  @Test
  public void completeCandidateAndExportPreserveTheAnalyzedInput() throws Exception {
    SnapshotRequest original = Fixtures.frrOspfBgp();
    putAndAwait("baseline", original);
    String candidate =
        original.devices().stream()
            .filter(d -> d.name().equals("r1"))
            .findFirst()
            .orElseThrow()
            .configText()
            .replaceAll("(?m)^[ \\t]*network 10\\.1\\.0\\.0/24[ \\t]*\\n", "");
    Response derive =
        call(
            "POST",
            "/snapshots/{id}/derive",
            "/snapshots/baseline/derive",
            new EngineServer.DeriveRequest(
                "candidate", List.of(new Change("r1", null, candidate))));
    awaitJob(derive.body().get("jobId").asText());
    Response routes =
        call(
            "POST",
            "/snapshots/{id}/questions/{question}",
            "/snapshots/candidate/questions/routes",
            Map.of("nodes", "r2", "network", "10.1.0.0/24"));
    assertThat(routes.body().path("answer").path("rows").size(), equalTo(0));

    HttpResponse<byte[]> exported =
        _http.send(
            HttpRequest.newBuilder(
                    URI.create(
                        "http://127.0.0.1:" + _server.port() + "/snapshots/candidate/export"))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofByteArray());
    assertThat(exported.statusCode(), equalTo(200));
    assertThat(
        exported.headers().firstValue("Content-Type").orElseThrow(), equalTo("application/zip"));
    assertThat(
        exported.headers().firstValue("X-Snapshot-SHA256").orElseThrow(),
        equalTo(Hashing.sha256().hashBytes(exported.body()).toString()));
    Map<String, String> files = new TreeMap<>();
    try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(exported.body()))) {
      ZipEntry entry;
      while ((entry = zip.getNextEntry()) != null) {
        files.put(
            entry.getName(),
            new String(zip.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
      }
    }
    assertThat(files.get("snapshot/configs/r1.cfg"), not(containsString("network 10.1.0.0/24")));
    assertThat(files.get("snapshot/configs/r1.cfg"), containsString("router bgp 65001"));
    assertThat(
        files.get("snapshot/configs/r2.cfg"),
        equalTo(
            InputComposer.compose(
                    original.devices().stream()
                        .filter(d -> d.name().equals("r2"))
                        .findFirst()
                        .orElseThrow())
                .text()));
    assertThat(files.keySet(), hasItem("snapshot/batfish/layer1_topology.json"));
    assertThat(_service.exportSnapshot("candidate"), equalTo(exported.body()));
  }

  @Test
  public void unmodeledChangeIsReportedAsIntroducedHoles() throws Exception {
    putAndAwait("live", Fixtures.frrOspfBgp());

    Response derive =
        call(
            "POST",
            "/snapshots/{id}/derive",
            "/snapshots/live/derive",
            new EngineServer.DeriveRequest(
                "change-2",
                List.of(
                    new Change("r1", List.of("router bgp 65001", " neighbor 10.0.0.2 shutdown")))));
    awaitJob(derive.body().get("jobId").asText());

    Response proposed = call("GET", "/snapshots/{id}", "/snapshots/change-2", null);
    assertThat(proposed.body().get("introduced").size(), equalTo(1));
  }

  @Test
  public void refusals() throws Exception {
    assertThat(call("GET", "/snapshots/{id}", "/snapshots/nope", null).status(), equalTo(404));
    assertThat(call("GET", "/jobs/{id}", "/jobs/nope", null).status(), equalTo(404));
    putAndAwait("live", Fixtures.crpd());
    assertThat(
        call(
                "POST",
                "/snapshots/{id}/questions/{question}",
                "/snapshots/live/questions/routes",
                Map.of("noSuchParameter", 1))
            .status(),
        equalTo(400));
    assertThat(
        call(
                "POST",
                "/snapshots/{id}/derive",
                "/snapshots/live/derive",
                new EngineServer.DeriveRequest(
                    "change-x", List.of(new Change("r9", List.of("set x")))))
            .status(),
        equalTo(400));
  }

  @Test
  public void deleteRemovesTheSnapshot() throws Exception {
    putAndAwait("gone", Fixtures.crpd());

    assertThat(call("DELETE", "/snapshots/{id}", "/snapshots/gone", null).status(), equalTo(204));
    assertThat(call("GET", "/snapshots/{id}", "/snapshots/gone", null).status(), equalTo(404));
  }
}
