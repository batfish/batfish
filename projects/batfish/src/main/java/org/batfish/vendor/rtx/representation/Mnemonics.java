package org.batfish.vendor.rtx.representation;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpProtocol;

/**
 * The protocol and port mnemonics accepted in the arguments of {@code ip filter}.
 *
 * <p>The protocol table is complete. The port table is not: the RTX command reference documents
 * only a subset of the names the device accepts (it labels the list "一部"), so names observed in
 * real configurations but absent from the manual are included here as well.
 */
public final class Mnemonics {

  private static final Map<String, IpProtocol> PROTOCOLS =
      ImmutableMap.<String, IpProtocol>builder()
          .put("icmp", IpProtocol.ICMP)
          .put("tcp", IpProtocol.TCP)
          .put("udp", IpProtocol.UDP)
          .put("ipv6", IpProtocol.IPV6)
          .put("gre", IpProtocol.GRE)
          .put("esp", IpProtocol.ESP)
          .put("ah", IpProtocol.AHP)
          .put("icmp6", IpProtocol.IPV6_ICMP)
          .build();

  private static final Map<String, IntegerSpace> PORTS =
      ImmutableMap.<String, IntegerSpace>builder()
          // Documented in the command reference.
          .put("ftpdata", IntegerSpace.of(20))
          .put("ftp", IntegerSpace.builder().including(20).including(21).build())
          .put("telnet", IntegerSpace.of(23))
          .put("smtp", IntegerSpace.of(25))
          .put("domain", IntegerSpace.of(53))
          .put("gopher", IntegerSpace.of(70))
          .put("finger", IntegerSpace.of(79))
          .put("www", IntegerSpace.of(80))
          .put("pop3", IntegerSpace.of(110))
          .put("sunrpc", IntegerSpace.of(111))
          .put("ident", IntegerSpace.of(113))
          .put("nntp", IntegerSpace.of(119))
          .put("ntp", IntegerSpace.of(123))
          .put("snmp", IntegerSpace.of(161))
          .put("syslog", IntegerSpace.of(514))
          .put("printer", IntegerSpace.of(515))
          .put("talk", IntegerSpace.of(517))
          .put("route", IntegerSpace.of(520))
          .put("uucp", IntegerSpace.of(540))
          .put("submission", IntegerSpace.of(587))
          // Accepted by the device but not listed in the command reference.
          .put("netbios_ns", IntegerSpace.of(137))
          .put("netbios_dgm", IntegerSpace.of(138))
          .put("netbios_ssn", IntegerSpace.of(139))
          .put("bgp", IntegerSpace.of(179))
          .put("https", IntegerSpace.of(443))
          .build();

  /** Returns the IP protocol named by {@code mnemonic}, or a decimal protocol number. */
  public static Optional<IpProtocol> lookupProtocol(String mnemonic) {
    IpProtocol named = PROTOCOLS.get(mnemonic);
    if (named != null) {
      return Optional.of(named);
    }
    return parseUint(mnemonic, 255).map(IpProtocol::fromNumber);
  }

  /** Returns the ports named by {@code item}, which may be a mnemonic or a decimal port number. */
  public static Optional<IntegerSpace> lookupPort(String item) {
    IntegerSpace named = PORTS.get(item);
    if (named != null) {
      return Optional.of(named);
    }
    return parseUint(item, 65535).map(IntegerSpace::of);
  }

  /**
   * Returns the single port named by {@code item}, for use as an endpoint of a range. Mnemonics
   * that stand for more than one port (e.g. {@code ftp}) cannot bound a range.
   */
  public static Optional<Integer> lookupPortEndpoint(String item) {
    return lookupPort(item).filter(IntegerSpace::isSingleton).map(IntegerSpace::singletonValue);
  }

  private static Optional<Integer> parseUint(String text, int max) {
    if (text.isEmpty() || !text.chars().allMatch(c -> c >= '0' && c <= '9')) {
      return Optional.empty();
    }
    try {
      int value = Integer.parseInt(text);
      return value <= max ? Optional.of(value) : Optional.empty();
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

  private Mnemonics() {}
}
