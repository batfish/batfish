package org.batfish.engine;

import java.util.Locale;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Part of a device's configuration that Batfish did not fully model. Batfish is closed-world: what
 * it does not model, it treats as absent. A hole tells NOCFlow which answers touching this device
 * must become {@code unknown} rather than trusting the absence.
 *
 * @param node lowered node name
 * @param family which fact family is affected; {@link #UNCLASSIFIED} affects every family
 * @param kind {@code unparsed} (whole device), {@code unrecognized} (line not in the grammar),
 *     {@code unimplemented} (parsed, not modeled), {@code redflag} (modeled with caveats), or
 *     {@code undefined-reference} (a policy or list referenced but never defined)
 * @param line 1-based line in the composed input, when known
 * @param text the offending text or message
 */
public record CoverageHole(
    @Nonnull String node,
    @Nonnull String family,
    @Nonnull String kind,
    @Nullable Integer line,
    @Nonnull String text) {

  public static final String INTERFACES = "interfaces";
  public static final String BGP = "bgp";
  public static final String OSPF = "ospf";
  public static final String STATIC = "static";
  public static final String POLICY = "policy";
  public static final String ACL = "acl";
  public static final String NAT = "nat";

  /** Management-plane configuration: never affects forwarding answers. */
  public static final String MANAGEMENT = "management";

  public static final String UNCLASSIFIED = "unclassified";

  // Only known management commands qualify. A word boundary also matches a hyphen,
  // so a bare "service" alternative would incorrectly exempt service-policy.
  private static final Pattern MANAGEMENT_TEXT =
      Pattern.compile(
          "^\\s*(no\\s+)?(lldp|cdp|username|user|enable\\s+(secret|password)|snmp-server|snmp"
              + "|ntp|logging|log|aaa|tacacs|radius|banner|clock|management"
              + "|service\\s+(password-encryption|timestamps|sequence-numbers|compress-config)"
              + "|line|ip\\s+(domain|name-server|ssh|http)|domain-name|name-server|motd"
              + "|hostname|terminal|alias)(?=\\s|$)");

  // EOS CLI validation policy, not interface speed/FEC or forwarding state.
  // Keep the warning visible; only these exact guard commands are non-forwarding.
  private static final Pattern L1_CLI_GUARD =
      Pattern.compile("^unsupported (speed|error-correction) action (error|warn)$");

  private static final Pattern BGP_TEXT = Pattern.compile("\\b(bgp|neighbor|peer-as|evpn)\\b");
  private static final Pattern OSPF_TEXT = Pattern.compile("\\bospf");
  private static final Pattern POLICY_TEXT =
      Pattern.compile(
          "route-map|prefix-list|community|as-path|policy-statement|policy-options"
              + "|route-filter|service-policy|policy-map");
  private static final Pattern ACL_TEXT =
      Pattern.compile("access-list|access-group|firewall|\\bfilter\\b|\\bacl\\b");
  private static final Pattern NAT_TEXT = Pattern.compile("\\bnat\\b");
  private static final Pattern STATIC_TEXT = Pattern.compile("ip route|\\bstatic\\b");
  private static final Pattern INTERFACE_TEXT =
      Pattern.compile("^\\s*interface|switchport|\\bvlan\\b|\\baddress\\b|channel-group|\\bvrf\\b");

  /**
   * The fact family a warning's text belongs to. The order matters: a line inside {@code router
   * bgp} that mentions a route-map is a policy hole, since that is what is not modeled.
   */
  public static @Nonnull String classify(@Nonnull String text) {
    String t = text.toLowerCase(Locale.ROOT);
    if (MANAGEMENT_TEXT.matcher(t).find() || L1_CLI_GUARD.matcher(t.strip()).matches()) {
      return MANAGEMENT;
    }
    if (POLICY_TEXT.matcher(t).find()) {
      return POLICY;
    }
    if (NAT_TEXT.matcher(t).find()) {
      return NAT;
    }
    if (ACL_TEXT.matcher(t).find()) {
      return ACL;
    }
    if (BGP_TEXT.matcher(t).find()) {
      return BGP;
    }
    if (OSPF_TEXT.matcher(t).find()) {
      return OSPF;
    }
    if (STATIC_TEXT.matcher(t).find()) {
      return STATIC;
    }
    if (INTERFACE_TEXT.matcher(t).find()) {
      return INTERFACES;
    }
    return UNCLASSIFIED;
  }
}
