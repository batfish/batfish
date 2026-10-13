package org.batfish.engine;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.SortedMap;
import java.util.function.Supplier;
import org.batfish.engine.SnapshotRequest.Device;
import org.batfish.engine.SnapshotRequest.Environment;
import org.batfish.engine.SnapshotRequest.Link;
import org.batfish.engine.SnapshotRequest.LinuxInterface;

/** Lab configs from NOCFlow's containerlab topologies, as snapshot requests. */
final class Fixtures {

  private Fixtures() {}

  /**
   * Config text variant: "" for the original, ".redacted" for the same file passed through
   * NOCFlow's relay redactor (regenerate with the redactor if a fixture changes).
   */
  private static String variant = "";

  /** Runs {@code body} with every fixture read from its {@code suffix} variant. */
  static <T> T withVariant(String suffix, Supplier<T> body) {
    String previous = variant;
    variant = suffix;
    try {
      return body.get();
    } finally {
      variant = previous;
    }
  }

  private static String config(String path) {
    return text(path + variant);
  }

  static String text(String path) {
    try (InputStream in =
        Fixtures.class.getResourceAsStream("/org/batfish/engine/fixtures/" + path)) {
      if (in == null) {
        throw new IllegalArgumentException("no fixture " + path);
      }
      return new String(in.readAllBytes(), UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** dc1-eos: cEOS core1 and core2, OSPF area 0 over Ethernet48 (10.255.0.0/31). */
  static SnapshotRequest dc1Eos() {
    return new SnapshotRequest(
        ImmutableList.of(
            new Device("core1", Dialect.ARISTA_EOS, config("dc1-eos/core1.cfg")),
            new Device("core2", Dialect.ARISTA_EOS, config("dc1-eos/core2.cfg"))),
        ImmutableList.of(new Link("core1", "Ethernet48", "core2", "Ethernet48")),
        Environment.NONE);
  }

  /** eos-policy: edge1 alone, exporting through route-map EXPORT-SITE; one unmodeled BGP knob. */
  static SnapshotRequest eosPolicy() {
    return new SnapshotRequest(
        ImmutableList.of(new Device("edge1", Dialect.ARISTA_EOS, config("eos-policy/edge1.cfg"))),
        ImmutableList.of(),
        Environment.NONE);
  }

  /** Every fixture by name, for goldens. */
  static SortedMap<String, SnapshotRequest> all() {
    return ImmutableSortedMap.of(
        "dc1-eos", dc1Eos(),
        "crpd", crpd(),
        "frr-ospf-bgp", frrOspfBgp(),
        "eos-policy", eosPolicy());
  }

  /** juniper-crpd: r1 (AS65001) and r2 (AS65002), eBGP over 10.0.0.0/30, export policies. */
  static SnapshotRequest crpd() {
    return new SnapshotRequest(
        ImmutableList.of(
            new Device("r1", Dialect.JUNOS, config("crpd/r1.set")),
            new Device("r2", Dialect.JUNOS, config("crpd/r2.set"))),
        ImmutableList.of(),
        Environment.NONE);
  }

  /**
   * frr-ospf-bgp: frr-wan with OSPF added. r1 (AS65001) and r2 (AS65002) run eBGP and OSPF over
   * 10.0.0.0/30; r1 exports through route-map EXPORT-SITE.
   */
  static SnapshotRequest frrOspfBgp() {
    return frrOspfBgp(Environment.NONE);
  }

  static SnapshotRequest frrOspfBgp(Environment environment) {
    return new SnapshotRequest(
        ImmutableList.of(
            new Device("r1", Dialect.FRR_LINUX, config("frr-ospf-bgp/r1.frr"), r1Interfaces()),
            new Device("r2", Dialect.FRR_LINUX, config("frr-ospf-bgp/r2.frr"), r2Interfaces())),
        ImmutableList.of(new Link("r1", "eth2", "r2", "eth1")),
        environment);
  }

  /** frr-ospf-bgp with a BGP password and OSPF MD5 keys on both routers. */
  static SnapshotRequest frrOspfBgpAuth() {
    return new SnapshotRequest(
        ImmutableList.of(
            new Device("r1", Dialect.FRR_LINUX, config("frr-ospf-bgp/r1-auth.frr"), r1Interfaces()),
            new Device(
                "r2", Dialect.FRR_LINUX, config("frr-ospf-bgp/r2-auth.frr"), r2Interfaces())),
        ImmutableList.of(new Link("r1", "eth2", "r2", "eth1")),
        Environment.NONE);
  }

  static List<LinuxInterface> r1Interfaces() {
    return ImmutableList.of(
        new LinuxInterface("lo", ImmutableList.of("1.1.1.1/32"), true),
        new LinuxInterface("eth1", ImmutableList.of("10.1.0.1/24"), true),
        new LinuxInterface("eth2", ImmutableList.of("10.0.0.1/30"), true));
  }

  static List<LinuxInterface> r2Interfaces() {
    return ImmutableList.of(
        new LinuxInterface("lo", ImmutableList.of("2.2.2.2/32"), true),
        new LinuxInterface("eth1", ImmutableList.of("10.0.0.2/30"), true),
        new LinuxInterface("eth2", ImmutableList.of("10.2.0.1/24"), true));
  }
}
