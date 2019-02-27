package org.batfish.datamodel.vendor_family.f5_bigip;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;

/** Configuration for a pool of virtual services. */
@ParametersAreNonnullByDefault
public final class Virtual implements Serializable {

  private static final String PROP_DESTINATION = "destination";
  private static final String PROP_DESTINATION_PORT = "destinationPort";
  private static final String PROP_MASK = "mask";
  private static final String PROP_MASK6 = "mask6";
  private static final String PROP_NAME = "name";
  private static final String PROP_POOL = "pool";
  private static final String PROP_SOURCE = "source";
  private static final String PROP_SOURCE_ADDRESS_TRANSLATION_POOL = "sourceAddressTranslationPool";
  private static final String PROP_SOURCE6 = "source6";
  private static final String PROP_VLANS = "vlans";
  private static final String PROP_VLANS_ENABLED = "vlansEnabled";
  private static final long serialVersionUID = 1L;

  @JsonCreator
  private static @Nonnull Virtual create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_VLANS) @Nullable Set<String> vlans) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    return new Virtual(name, ImmutableSet.copyOf(firstNonNull(vlans, ImmutableSet.of())));
  }

  private @Nullable String _destination;
  private @Nullable Integer _destinationPort;
  private @Nullable Ip _mask;
  private @Nullable Ip6 _mask6;
  private final @Nonnull String _name;
  private @Nullable String _pool;
  private @Nullable Prefix _source;
  private @Nullable Prefix6 _source6;
  private @Nullable String _sourceAddressTranslationPool;
  private final @Nonnull Set<String> _vlans;
  private boolean _vlansEnabled;

  public Virtual(String name) {
    _name = name;
    _vlans = new HashSet<>();
  }

  private Virtual(String name, Set<String> vlans) {
    _name = name;
    _vlans = vlans;
  }

  @JsonProperty(PROP_DESTINATION)
  public @Nullable String getDestination() {
    return _destination;
  }

  @JsonProperty(PROP_DESTINATION_PORT)
  public @Nullable Integer getDestinationPort() {
    return _destinationPort;
  }

  @JsonProperty(PROP_MASK)
  public @Nullable Ip getMask() {
    return _mask;
  }

  @JsonProperty(PROP_MASK6)
  public @Nullable Ip6 getMask6() {
    return _mask6;
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @JsonProperty(PROP_POOL)
  public @Nullable String getPool() {
    return _pool;
  }

  @JsonProperty(PROP_SOURCE)
  public @Nullable Prefix getSource() {
    return _source;
  }

  @JsonProperty(PROP_SOURCE6)
  public @Nullable Prefix6 getSource6() {
    return _source6;
  }

  @JsonProperty(PROP_SOURCE_ADDRESS_TRANSLATION_POOL)
  public @Nullable String getSourceAddressTranslationPool() {
    return _sourceAddressTranslationPool;
  }

  @JsonProperty(PROP_VLANS)
  public @Nonnull Set<String> getVlans() {
    return _vlans;
  }

  @JsonProperty(PROP_VLANS_ENABLED)
  public boolean getVlansEnabled() {
    return _vlansEnabled;
  }

  @JsonProperty(PROP_DESTINATION)
  public void setDestination(@Nullable String destination) {
    _destination = destination;
  }

  @JsonProperty(PROP_DESTINATION_PORT)
  public void setDestinationPort(@Nullable Integer destinationPort) {
    _destinationPort = destinationPort;
  }

  @JsonProperty(PROP_MASK)
  public void setMask(@Nullable Ip mask) {
    _mask = mask;
  }

  @JsonProperty(PROP_MASK6)
  public void setMask6(@Nullable Ip6 mask6) {
    _mask6 = mask6;
  }

  @JsonProperty(PROP_POOL)
  public void setPool(@Nullable String pool) {
    _pool = pool;
  }

  @JsonProperty(PROP_SOURCE)
  public void setSource(@Nullable Prefix source) {
    _source = source;
  }

  @JsonProperty(PROP_SOURCE6)
  public void setSource6(@Nullable Prefix6 source6) {
    _source6 = source6;
  }

  @JsonProperty(PROP_SOURCE_ADDRESS_TRANSLATION_POOL)
  public void setSourceAddressTranslationPool(@Nullable String sourceAddressTranslationPool) {
    _sourceAddressTranslationPool = sourceAddressTranslationPool;
  }

  @JsonProperty(PROP_VLANS_ENABLED)
  public void setVlansEnabled(boolean vlansEnabled) {
    _vlansEnabled = vlansEnabled;
  }
}
