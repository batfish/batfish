package org.batfish.grammar.flatjuniper;

import javax.annotation.Nonnull;

/**
 * Utility class defining Juniper list-like paths, e.g. paths in the Juniper hierarchy to a node
 * whose children are elements of an ordered list.
 */
public final class JuniperListPaths {

  public static @Nonnull String[] getJuniperListPaths() {
    return JUNIPER_LIST_PATHS;
  }

  // These should be tested, in "FlatJuniperGrammarTest#testApplyGroupsLists"
  private static final String[] JUNIPER_LIST_PATHS =
      new String[] {
        // The last space-separated word of each item corresponds to a Juniper list node, i.e.
        // a node for which the  order of its children matters.
        // Currently, only literals and '<*>' are supported, where '<*>' matches any hierarchy
        // path node.
        "firewall family inet filter <*> term",
        "firewall filter <*> term",
        "interfaces <*> unit <*> family inet filter input-list",
        "interfaces <*> unit <*> family inet filter output-list",
        "policy-options policy-statement <*> term",
        "protocols bgp group <*> export",
        "protocols bgp group <*> import",
        "security nat destination rule-set <*> rule",
        // https://www.juniper.net/documentation/us/en/software/junos/nat/topics/ref/statement/security-edit-source-address-name-nat-destination.html
        "security nat destination rule-set <*> rule <*> match source-address-name",
        "security nat source rule-set <*> rule",
        // https://www.juniper.net/documentation/us/en/software/junos/nat/topics/ref/statement/security-edit-source-address-name-nat-source.html
        "security nat source rule-set <*> rule <*> match source-address-name",
        "security policies from-zone <*> to-zone <*> policy",
        "system domain-search"
      };

  private JuniperListPaths() {}
}
