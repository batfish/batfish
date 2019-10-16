package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/**
 * Configuration of BGP within a virtual-router. Config at {@code network virtual-router NAME
 * protocol bgp}.
 */
public class BgpVr implements Serializable {
  /** From PAN admin UI - only shows in running config if checked (as yes). */
  private static final boolean DEFAULT_ENABLE = false;
  /** From PAN admin UI - only shows in running config if checked (as yes). */
  private static final boolean DEFAULT_INSTALL_ROUTE = false;
  /** From PAN admin UI - only shows in running config if unchecked (as no). */
  private static final boolean DEFAULT_REJECT_DEFAULT_ROUTE = true;

  public BgpVr() {
    _enable = DEFAULT_ENABLE;
    _importPolicyRules = new HashMap<>();
    _exportPolicyRules = new HashMap<>();
    _installRoute = DEFAULT_INSTALL_ROUTE;
    _peerGroups = new HashMap<>(0);
    _redistRules = new HashMap<>(0);
    _rejectDefaultRoute = DEFAULT_REJECT_DEFAULT_ROUTE;
    _routingOptions = new BgpVrRoutingOptions();
  }

  public boolean getEnable() {
    return _enable;
  }

  public void setEnable(boolean enable) {
    _enable = enable;
  }

  public Map<String, PolicyRule> getExportPolicyRules() {
    return _exportPolicyRules;
  }

  public PolicyRule getOrCreateExportPolicyRule(String name) {
    return _exportPolicyRules.computeIfAbsent(name, PolicyRule::new);
  }

  public Map<String, PolicyRule> getImportPolicyRules() {
    return _importPolicyRules;
  }

  public PolicyRule getOrCreateImportPolicyRule(String name) {
    return _importPolicyRules.computeIfAbsent(name, PolicyRule::new);
  }

  public @Nonnull Map<RedistRuleRefNameOrPrefix, RedistRule> getRedistRules() {
    return _redistRules;
  }

  public @Nonnull RedistRule getOrCreateRedistRule(
      RedistRuleRefNameOrPrefix redistRuleRefNameOrPrefix) {
    return _redistRules.computeIfAbsent(
        redistRuleRefNameOrPrefix, redistRuleRefNameOrPrefix1 -> new RedistRule());
  }

  public boolean getInstallRoute() {
    return _installRoute;
  }

  public void setInstallRoute(boolean installRoute) {
    _installRoute = installRoute;
  }

  public @Nullable Long getLocalAs() {
    return _localAs;
  }

  public void setLocalAs(@Nullable Long localAs) {
    _localAs = localAs;
  }

  public @Nonnull BgpPeerGroup getOrCreatePeerGroup(String name) {
    return _peerGroups.computeIfAbsent(name, BgpPeerGroup::new);
  }

  public @Nonnull Map<String, BgpPeerGroup> getPeerGroups() {
    return Collections.unmodifiableMap(_peerGroups);
  }

  public boolean getRejectDefaultRoute() {
    return _rejectDefaultRoute;
  }

  public void setRejectDefaultRoute(boolean rejectDefaultRoute) {
    _rejectDefaultRoute = rejectDefaultRoute;
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  public @Nonnull BgpVrRoutingOptions getRoutingOptions() {
    return _routingOptions;
  }

  // private implementation details
  private final BgpVrRoutingOptions _routingOptions;

  private boolean _enable;
  private Map<String, PolicyRule> _exportPolicyRules;
  private boolean _installRoute;
  private Map<String, PolicyRule> _importPolicyRules;
  private @Nullable Long _localAs;
  private @Nonnull final Map<String, BgpPeerGroup> _peerGroups;
  /**
   * Redist rules used by this BGP process, keyed by the referred redist profile name or by the
   * prefix used for filtering
   */
  private @Nonnull final Map<RedistRuleRefNameOrPrefix, RedistRule> _redistRules;

  private boolean _rejectDefaultRoute;
  private @Nullable Ip _routerId;
}
