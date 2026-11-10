package org.batfish.vendor.rtx.representation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;

/**
 * Parsers for the arguments of {@code ip filter}, whose syntax the lexer does not decompose.
 *
 * <p>Every method returns {@link Optional#empty} rather than throwing when the argument cannot be
 * interpreted, so that the caller can attribute a warning to the offending line. The wildcard
 * {@code *} is not handled here; callers check for it before parsing.
 */
public final class RtxFilterSpecs {

  /** Protocol keyword that matches TCP packets with the ACK flag set. */
  public static final String ESTABLISHED = "established";

  /**
   * Parses an address argument: a comma-separated list whose items are each an address, a prefix,
   * or an inclusive range of addresses.
   */
  public static Optional<IpSpace> parseAddress(String spec) {
    List<IpSpace> spaces = new ArrayList<>();
    for (String item : spec.split(",", -1)) {
      Optional<IpSpace> space = parseAddressItem(item);
      if (!space.isPresent()) {
        return Optional.empty();
      }
      spaces.add(space.get());
    }
    // union() returns null only for an empty iterable, which split() cannot produce.
    return Optional.ofNullable(AclIpSpace.union(spaces));
  }

  private static Optional<IpSpace> parseAddressItem(String item) {
    int dash = item.indexOf('-');
    if (dash > 0) {
      Optional<Ip> low = parseIp(item.substring(0, dash));
      Optional<Ip> high = parseIp(item.substring(dash + 1));
      if (!low.isPresent() || !high.isPresent()) {
        return Optional.empty();
      }
      if (low.get().asLong() > high.get().asLong()) {
        return Optional.empty();
      }
      return Optional.of(IpRange.range(low.get(), high.get()));
    }
    int slash = item.indexOf('/');
    if (slash > 0) {
      Optional<Ip> ip = parseIp(item.substring(0, slash));
      Optional<Integer> length = parseUint(item.substring(slash + 1), Prefix.MAX_PREFIX_LENGTH);
      if (!ip.isPresent() || !length.isPresent()) {
        // Netmasks written in dotted-decimal or hexadecimal are legal but not supported here.
        return Optional.empty();
      }
      return Optional.of(Prefix.create(ip.get(), length.get()).toIpSpace());
    }
    return parseIp(item).map(Ip::toIpSpace);
  }

  private static Optional<Ip> parseIp(String text) {
    String[] octets = text.split("\\.", -1);
    if (octets.length != 4) {
      return Optional.empty();
    }
    long value = 0;
    for (String octet : octets) {
      Optional<Integer> parsed = parseUint(octet, 255);
      if (!parsed.isPresent()) {
        return Optional.empty();
      }
      value = (value << 8) | parsed.get();
    }
    return Optional.of(Ip.create(value));
  }

  /**
   * Parses a protocol argument: a comma-separated list of protocol mnemonics or numbers, or the
   * single keyword {@code established}.
   *
   * <p>Returns the set of protocols matched. {@code established} is reported separately by {@link
   * #isEstablished}; for it the protocol set is TCP.
   */
  public static Optional<Set<IpProtocol>> parseProtocols(String spec) {
    if (spec.equals(ESTABLISHED)) {
      return Optional.of(ImmutableSet.of(IpProtocol.TCP));
    }
    ImmutableSet.Builder<IpProtocol> protocols = ImmutableSet.builder();
    for (String item : spec.split(",", -1)) {
      Optional<IpProtocol> protocol = Mnemonics.lookupProtocol(item);
      if (!protocol.isPresent()) {
        // Includes the TCP-flag and ICMP-class keywords (tcpsyn, icmp-error, tcpflag=..., ...),
        // which are not modeled.
        return Optional.empty();
      }
      protocols.add(protocol.get());
    }
    return Optional.of(protocols.build());
  }

  /** Whether a protocol argument is the {@code established} keyword. */
  public static boolean isEstablished(String spec) {
    return spec.equals(ESTABLISHED);
  }

  /**
   * Parses a port argument: a comma-separated list whose items are each a port (as a mnemonic or a
   * number) or a range of ports. A range endpoint may be omitted to mean the lowest or highest
   * port.
   */
  public static Optional<IntegerSpace> parsePorts(String spec) {
    return parseIntegerList(spec, 0, 65535);
  }

  /** Parses an ICMP type or code argument, which uses the same syntax as a port argument. */
  public static Optional<IntegerSpace> parseIcmpTypesOrCodes(String spec) {
    return parseIntegerList(spec, 0, 255);
  }

  private static Optional<IntegerSpace> parseIntegerList(String spec, int min, int max) {
    IntegerSpace.Builder builder = IntegerSpace.builder();
    for (String item : spec.split(",", -1)) {
      Optional<Range<Integer>> range = parseIntegerItem(item, min, max);
      if (!range.isPresent()) {
        return Optional.empty();
      }
      builder.including(range.get());
    }
    return Optional.of(builder.build());
  }

  private static Optional<Range<Integer>> parseIntegerItem(String item, int min, int max) {
    int dash = item.indexOf('-');
    if (dash < 0) {
      return resolveEndpoint(item, max).map(Range::singleton);
    }
    String lowText = item.substring(0, dash);
    String highText = item.substring(dash + 1);
    Integer low = min;
    Integer high = max;
    if (!lowText.isEmpty()) {
      Optional<Integer> parsed = resolveEndpoint(lowText, max);
      if (!parsed.isPresent()) {
        return Optional.empty();
      }
      low = parsed.get();
    }
    if (!highText.isEmpty()) {
      Optional<Integer> parsed = resolveEndpoint(highText, max);
      if (!parsed.isPresent()) {
        return Optional.empty();
      }
      high = parsed.get();
    }
    if (low > high) {
      return Optional.empty();
    }
    return Optional.of(Range.closed(low, high));
  }

  private static Optional<Integer> resolveEndpoint(String item, int max) {
    return Mnemonics.lookupPortEndpoint(item).filter(port -> port <= max);
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

  /** Returns the arguments of an {@code ip filter} line, padded with {@code *} to {@code size}. */
  public static List<String> padArguments(List<String> args, int size) {
    ImmutableList.Builder<String> padded = ImmutableList.builder();
    padded.addAll(args);
    for (int i = args.size(); i < size; i++) {
      padded.add("*");
    }
    return padded.build();
  }

  private RtxFilterSpecs() {}
}
