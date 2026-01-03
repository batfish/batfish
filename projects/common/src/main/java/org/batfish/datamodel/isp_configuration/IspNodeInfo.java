package org.batfish.datamodel.isp_configuration;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.util.isp.IspModelingUtils.getDefaultIspNodeName;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.isp_configuration.traffic_filtering.IspTrafficFiltering;

/** Information about auto-generated ISP node */
@ParametersAreNonnullByDefault
public class IspNodeInfo {
  private static final String PROP_ASN = "asn";
  private static final String PROP_NAME = "name";
  private static final String PROP_ROLE = "role";
  private static final String PROP_ADDITIONAL_ANNOUNCEMENTS_TO_INTERNET =
      "additionalAnnouncementsToInternet";
  private static final String PROP_TRAFFIC_FILTERING = "trafficFiltering";

  /** Dictates certain default behaviors of this ISP node */
  public enum Role {
    /**
     * The ISP provides transit to the Internet. It connects to the Internet and propagates the
     * default route from there. It does not propagate any communities and blocks reserved address
     * traffic (unless overriden by PROP_TRAFFIC_FILTERING) .
     */
    TRANSIT,
    /**
     * The ISP mimics a private backbone. It does not connect to the Internet and so does not
     * provide the default route. It propagates communities and extended communities and does not
     * filter any traffic (unless overridden by PROP_TRAFFIC_FILTERING).
     */
    PRIVATE_BACKBONE
  }

  private final long _asn;
  private final @Nonnull String _name;
  private final @Nonnull Role _role;
  private final @Nonnull List<IspAnnouncement> _additionalAnnouncement;
  private final @Nullable IspTrafficFiltering _trafficFiltering;

  public IspNodeInfo(long asn, String name) {
    this(asn, name, Role.TRANSIT, ImmutableList.of(), null);
  }

  public IspNodeInfo(long asn, String name, List<IspAnnouncement> additionalAnnouncements) {
    this(asn, name, Role.TRANSIT, additionalAnnouncements, null);
  }

  public IspNodeInfo(
      long asn,
      String name,
      Role role,
      List<IspAnnouncement> additionalAnnouncements,
      @Nullable IspTrafficFiltering trafficFiltering) {
    checkArgument(
        role == Role.TRANSIT || additionalAnnouncements.isEmpty(),
        "%s should not be provided unless role is TRANSIT",
        PROP_ADDITIONAL_ANNOUNCEMENTS_TO_INTERNET);
    _asn = asn;
    _name = name;
    _role = role;
    _additionalAnnouncement = additionalAnnouncements;
    _trafficFiltering = trafficFiltering;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IspNodeInfo)) {
      return false;
    }
    IspNodeInfo that = (IspNodeInfo) o;
    return _asn == that._asn
        && _name.equals(that._name)
        && _role == that._role
        && _additionalAnnouncement.equals(that._additionalAnnouncement)
        && Objects.equals(_trafficFiltering, that._trafficFiltering);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_asn, _name, _role, _additionalAnnouncement, _trafficFiltering);
  }

  @JsonCreator
  private static IspNodeInfo jsonCreator(
      @JsonProperty(PROP_ASN) @Nullable Long asn,
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_ROLE) @Nullable Role role,
      @JsonProperty(PROP_ADDITIONAL_ANNOUNCEMENTS_TO_INTERNET) @Nullable
          List<IspAnnouncement> additionalAnnouncements,
      @JsonProperty(PROP_TRAFFIC_FILTERING) @Nullable IspTrafficFiltering trafficFiltering) {
    checkArgument(asn != null, "Missing %", PROP_ASN);
    return new IspNodeInfo(
        asn,
        firstNonNull(name, getDefaultIspNodeName(asn)),
        firstNonNull(role, Role.TRANSIT),
        firstNonNull(additionalAnnouncements, ImmutableList.of()),
        trafficFiltering);
  }

  @JsonProperty(PROP_ASN)
  public long getAsn() {
    return _asn;
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @JsonProperty(PROP_ROLE)
  public Role getRole() {
    return _role;
  }

  @JsonProperty(PROP_ADDITIONAL_ANNOUNCEMENTS_TO_INTERNET)
  public @Nonnull List<IspAnnouncement> getAdditionalAnnouncements() {
    return _additionalAnnouncement;
  }

  @JsonProperty(PROP_TRAFFIC_FILTERING)
  public @Nullable IspTrafficFiltering getIspTrafficFiltering() {
    return _trafficFiltering;
  }
}
