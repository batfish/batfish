package org.batfish.vendor.sonic.representation;

import static org.batfish.common.Warnings.TAG_RED_FLAG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.junit.Test;

public class ResolveConfTest {

  @Test
  public void testDeserialize_validIps() {
    List<String> lines =
        ImmutableList.of(
            "garbage garbage",
            "nameserver 1.1.1.1",
            "nameserver 2.2.2.2",
            "nameserver 2000::100:a00:20ff:de8a:643a",
            "nameserver 2001::100:a00:20ff:de8a:643a");

    Warnings warnings = new Warnings(true, true, true);
    ResolvConf resolveConf = ResolvConf.deserialize(String.join("\n", lines), warnings);

    assertEquals(
        ImmutableList.of(Ip.parse("1.1.1.1"), Ip.parse("2.2.2.2")), resolveConf.getNameservers());
    assertEquals(
        ImmutableList.of(
            Ip6.parse("2000::100:a00:20ff:de8a:643a"), Ip6.parse("2001::100:a00:20ff:de8a:643a")),
        resolveConf.getNameservers6());
    assertTrue(warnings.isEmpty());
  }

  @Test
  public void testDeserialize_invalidIp() {
    List<String> lines = ImmutableList.of("nameserver garbage");

    Warnings warnings = new Warnings(true, true, true);
    ResolvConf resolveConf = ResolvConf.deserialize(String.join("\n", lines), warnings);

    assertEquals(ImmutableList.of(), resolveConf.getNameservers());
    assertEquals(ImmutableList.of(), resolveConf.getNameservers6());
    assertEquals(
        ImmutableSortedSet.of(
            new Warning("'garbage' is neither IPv4 nor IPv6 address", TAG_RED_FLAG)),
        warnings.getRedFlagWarnings());
  }

  @Test
  public void testDeserialize_noNameserverLine() {
    List<String> lines = ImmutableList.of("garbage garbage");

    Warnings warnings = new Warnings(true, true, true);
    ResolvConf resolveConf = ResolvConf.deserialize(String.join("\n", lines), warnings);

    assertEquals(ImmutableList.of(), resolveConf.getNameservers());
    assertEquals(ImmutableList.of(), resolveConf.getNameservers6());
    assertEquals(
        ImmutableSortedSet.of(new Warning("No nameserver found", TAG_RED_FLAG)),
        warnings.getRedFlagWarnings());
  }

  @Test
  public void testDeserialize_extraWhitespaceAndComments() {
    // leading space; tabs; training space
    List<String> lines = ImmutableList.of(" nameserver      1.1.1.1   # comment");

    Warnings warnings = new Warnings(true, true, true);
    ResolvConf resolveConf = ResolvConf.deserialize(String.join("\n", lines), warnings);

    assertEquals(ImmutableList.of(Ip.parse("1.1.1.1")), resolveConf.getNameservers());
  }
}
