package org.batfish.engine;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.engine.Deriver.Change;
import org.batfish.engine.EngineService.EngineException;
import org.batfish.version.BatfishVersion;

/**
 * The engine's HTTP front end, as specified by {@code openapi.yaml}. A thin layer: it parses
 * requests strictly (unknown properties are rejected), calls {@link EngineService}, and maps
 * refusals to status codes.
 */
public final class EngineServer implements AutoCloseable {

  private static final Logger LOGGER = LogManager.getLogger(EngineServer.class);

  /** The engine's own version, reported by {@code /health}. */
  public static final String ENGINE_VERSION = "1.6.0";

  record DeriveRequest(@Nonnull String id, @Nonnull List<Change> changes) {}

  record DiffRequest(
      @Nonnull String base,
      @Nonnull String proposed,
      @Nonnull String question,
      @Nullable JsonNode parameters) {}

  private static final Pattern SNAPSHOT = Pattern.compile("^/snapshots/([^/]+)$");
  private static final Pattern FACTS = Pattern.compile("^/snapshots/([^/]+)/facts$");
  private static final Pattern EXPORT = Pattern.compile("^/snapshots/([^/]+)/export$");
  private static final Pattern DERIVE = Pattern.compile("^/snapshots/([^/]+)/derive$");
  private static final Pattern QUESTION = Pattern.compile("^/snapshots/([^/]+)/questions/([^/]+)$");
  private static final Pattern JOB = Pattern.compile("^/jobs/([^/]+)$");

  /** Requests are strict; responses omit nulls but keep empty lists, as the contract requires. */
  private static final ObjectMapper READER = BatfishObjectMapper.mapper();

  private static final ObjectMapper WRITER =
      BatfishObjectMapper.mapper()
          .copy()
          .setDefaultPropertyInclusion(
              JsonInclude.Value.construct(
                  JsonInclude.Include.NON_NULL, JsonInclude.Include.ALWAYS));

  private final EngineService _service;
  private final HttpServer _server;
  private final ExecutorService _executor;

  public EngineServer(@Nonnull EngineService service, @Nonnull InetSocketAddress address)
      throws IOException {
    this(service, address, Executors.newFixedThreadPool(8));
  }

  /** The server owns and closes the supplied request executor. */
  EngineServer(EngineService service, InetSocketAddress address, ExecutorService executor)
      throws IOException {
    _service = service;
    _executor = executor;
    try {
      _server = HttpServer.create(address, 0);
    } catch (IOException | RuntimeException failure) {
      executor.shutdownNow();
      throw failure;
    }
    _server.createContext("/", this::handle);
    _server.setExecutor(_executor);
  }

  public void start() {
    _server.start();
  }

  public int port() {
    return _server.getAddress().getPort();
  }

  @Override
  public void close() {
    try {
      _server.stop(0);
    } finally {
      _executor.shutdownNow();
      try {
        if (!_executor.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS)) {
          throw new IllegalStateException("request workers did not stop");
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("interrupted while stopping request workers", e);
      }
    }
  }

  private void handle(HttpExchange ex) throws IOException {
    try (ex) {
      try {
        route(ex);
      } catch (EngineException e) {
        send(ex, e.status(), Map.of("error", e.getMessage()));
      } catch (JsonProcessingException | IllegalArgumentException e) {
        send(ex, 400, Map.of("error", String.valueOf(e.getMessage())));
      } catch (RuntimeException e) {
        LOGGER.error("request failed: {} {}", ex.getRequestMethod(), ex.getRequestURI(), e);
        send(ex, 500, Map.of("error", String.valueOf(e.getMessage())));
      }
    }
  }

  private void route(HttpExchange ex) throws IOException {
    String method = ex.getRequestMethod();
    String path = ex.getRequestURI().getPath();
    Matcher m;
    if (path.equals("/health") && method.equals("GET")) {
      send(
          ex,
          200,
          Map.of(
              "status",
              "ok",
              "batfishVersion",
              BatfishVersion.getVersionStatic(),
              "engineVersion",
              ENGINE_VERSION));
    } else if ((m = SNAPSHOT.matcher(path)).matches()) {
      String id = m.group(1);
      switch (method) {
        case "PUT" -> send(ex, 202, _service.put(id, read(ex, SnapshotRequest.class)));
        case "GET" -> send(ex, 200, _service.snapshot(id));
        case "DELETE" -> {
          _service.delete(id);
          ex.sendResponseHeaders(204, -1);
        }
        default -> notAllowed(ex);
      }
    } else if ((m = EXPORT.matcher(path)).matches() && method.equals("GET")) {
      byte[] bytes = _service.exportSnapshot(m.group(1));
      ex.getResponseHeaders().set("Content-Type", "application/zip");
      ex.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"snapshot.zip\"");
      ex.getResponseHeaders()
          .set("X-Snapshot-SHA256", Hashing.sha256().hashBytes(bytes).toString());
      ex.sendResponseHeaders(200, bytes.length);
      try (OutputStream out = ex.getResponseBody()) {
        out.write(bytes);
      }
    } else if ((m = FACTS.matcher(path)).matches() && method.equals("GET")) {
      send(ex, 200, _service.facts(m.group(1)));
    } else if ((m = DERIVE.matcher(path)).matches() && method.equals("POST")) {
      DeriveRequest req = read(ex, DeriveRequest.class);
      send(ex, 202, _service.derive(m.group(1), req.id(), req.changes()));
    } else if ((m = QUESTION.matcher(path)).matches() && method.equals("POST")) {
      send(ex, 200, _service.ask(m.group(1), m.group(2), readTree(ex)));
    } else if (path.equals("/diff") && method.equals("POST")) {
      DiffRequest req = read(ex, DiffRequest.class);
      send(ex, 200, _service.diff(req.base(), req.proposed(), req.question(), req.parameters()));
    } else if ((m = JOB.matcher(path)).matches() && method.equals("GET")) {
      send(ex, 200, _service.job(m.group(1)));
    } else {
      send(ex, 404, Map.of("error", "no route " + method + " " + path));
    }
  }

  private static <T> T read(HttpExchange ex, Class<T> type) throws IOException {
    try (InputStream in = ex.getRequestBody()) {
      T value = READER.readValue(in, type);
      if (value == null) {
        throw new IllegalArgumentException("request body required");
      }
      return value;
    }
  }

  private static @Nullable JsonNode readTree(HttpExchange ex) throws IOException {
    try (InputStream in = ex.getRequestBody()) {
      byte[] body = in.readAllBytes();
      return body.length == 0 ? null : READER.readTree(body);
    }
  }

  private static void notAllowed(HttpExchange ex) throws IOException {
    send(ex, 405, Map.of("error", "method not allowed"));
  }

  private static void send(HttpExchange ex, int status, Object body) throws IOException {
    byte[] bytes = WRITER.writeValueAsString(body).getBytes(UTF_8);
    ex.getResponseHeaders().set("Content-Type", "application/json");
    ex.sendResponseHeaders(status, bytes.length);
    try (OutputStream out = ex.getResponseBody()) {
      out.write(bytes);
    }
  }
}
