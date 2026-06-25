package org.batfish.datamodel.bgp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Describes which ingress VRF(s) a BGP listener's TCP socket will accept connections from, and
 * which VRF an initiator sources its outbound connection from.
 *
 * <p>The three states are mutually-exclusive answers to "which VRF is this peer's session in":
 *
 * <ul>
 *   <li>{@link OwnVrf}: the session uses the peer's own (configured) VRF. This is the default.
 *   <li>{@link SpecificVrf}: the session uses a specific, possibly different, VRF. For example,
 *       Junos {@code forwarding-context master} causes a BGP session in a VRF to source its TCP
 *       connection from the default routing instance.
 *   <li>{@link AnyVrf}: the listener's global socket accepts connections arriving on any VRF, as on
 *       Linux-based NOSes (FRR/Cumulus/SONiC) with the kernel sysctl {@code tcp_l3mdev_accept=1}.
 *       This is a listener-side concept only: an outbound (initiated) connection is still bound to
 *       one VRF, so origination collapses to the peer's own/config VRF.
 * </ul>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = SessionVrfScope.OwnVrf.class, name = "OwnVrf"),
  @JsonSubTypes.Type(value = SessionVrfScope.SpecificVrf.class, name = "SpecificVrf"),
  @JsonSubTypes.Type(value = SessionVrfScope.AnyVrf.class, name = "AnyVrf"),
})
public sealed interface SessionVrfScope extends Serializable
    permits SessionVrfScope.OwnVrf, SessionVrfScope.SpecificVrf, SessionVrfScope.AnyVrf {

  /** The concrete VRF an outbound (initiated) connection is sourced from. */
  @Nonnull
  String originVrf(String configVrf);

  /** The VRFs this listener registers under to accept inbound connections. */
  @Nonnull
  Set<String> listenVrfs(String configVrf, Set<String> allNodeVrfs);

  /**
   * Whether an inbound connection arriving on {@code ingressVrf} is acceptable by this listener.
   */
  boolean acceptsIngressVrf(String ingressVrf, String configVrf);

  /**
   * Value to display for the {@code Session_VRF} answer column / peer property. {@code null} for
   * {@link OwnVrf} (i.e., no override of the peer's own VRF).
   */
  @Nullable
  String displayString();

  /** The session uses the peer's own (configured) VRF. */
  record OwnVrf() implements SessionVrfScope {
    private static final OwnVrf INSTANCE = new OwnVrf();

    public static @Nonnull OwnVrf instance() {
      return INSTANCE;
    }

    @Override
    public @Nonnull String originVrf(String configVrf) {
      return configVrf;
    }

    @Override
    public @Nonnull Set<String> listenVrfs(String configVrf, Set<String> allNodeVrfs) {
      return ImmutableSet.of(configVrf);
    }

    @Override
    public boolean acceptsIngressVrf(String ingressVrf, String configVrf) {
      return ingressVrf.equals(configVrf);
    }

    @Override
    public @Nullable String displayString() {
      return null;
    }
  }

  /**
   * The session uses a specific, possibly different, VRF (e.g. Junos {@code forwarding-context}).
   */
  record SpecificVrf(@Nonnull String vrf) implements SessionVrfScope {
    @JsonCreator
    public SpecificVrf(@JsonProperty("vrf") @Nonnull String vrf) {
      this.vrf = vrf;
    }

    @Override
    @JsonProperty("vrf")
    public @Nonnull String vrf() {
      return vrf;
    }

    @Override
    public @Nonnull String originVrf(String configVrf) {
      return vrf;
    }

    @Override
    public @Nonnull Set<String> listenVrfs(String configVrf, Set<String> allNodeVrfs) {
      return ImmutableSet.of(vrf);
    }

    @Override
    public boolean acceptsIngressVrf(String ingressVrf, String configVrf) {
      return ingressVrf.equals(vrf);
    }

    @Override
    public @Nonnull String displayString() {
      return vrf;
    }
  }

  /**
   * The listener's global socket accepts connections arriving on any VRF ({@code
   * tcp_l3mdev_accept=1}). As an initiator, origination collapses to the peer's own/config VRF.
   */
  record AnyVrf() implements SessionVrfScope {
    private static final AnyVrf INSTANCE = new AnyVrf();

    public static @Nonnull AnyVrf instance() {
      return INSTANCE;
    }

    @Override
    public @Nonnull String originVrf(String configVrf) {
      return configVrf;
    }

    @Override
    public @Nonnull Set<String> listenVrfs(String configVrf, Set<String> allNodeVrfs) {
      return ImmutableSet.copyOf(allNodeVrfs);
    }

    @Override
    public boolean acceptsIngressVrf(String ingressVrf, String configVrf) {
      return true;
    }

    @Override
    public @Nonnull String displayString() {
      return "*";
    }
  }
}
