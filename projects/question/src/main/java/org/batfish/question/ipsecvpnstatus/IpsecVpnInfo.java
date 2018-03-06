package org.batfish.question.ipsecvpnstatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IpsecVpn;

public class IpsecVpnInfo implements Comparable<IpsecVpnInfo> {

  public enum Problem {
    INCOMPATIBLE_IKE_PROPOSALS,
    INCOMPATIBLE_IPSEC_PROPOSALS,
    INCOMPATIBLE_PRE_SHARED_KEY,
    MISSING_REMOTE_ENDPOINT,
    MULTIPLE_REMOTE_ENDPOINTS,
    NONE
  }

  private static final String PROP_IPSEC_VPN_ENDPOINT = "ipsecVpnEndpoint";
  private static final String PROP_PROBLEMS = "problems";
  private static final String PROP_REMOTE_ENDPOINT = "remoteEndpoint";

  private IpsecVpnEndpoint _ipsecVpnEndpoint;
  private SortedSet<Problem> _problems;
  private IpsecVpnEndpoint _remoteEndpoint;

  @JsonCreator
  private IpsecVpnInfo(
      @JsonProperty(PROP_IPSEC_VPN_ENDPOINT) IpsecVpnEndpoint vpn,
      @JsonProperty(PROP_PROBLEMS) SortedSet<Problem> problems,
      @JsonProperty(PROP_REMOTE_ENDPOINT) IpsecVpnEndpoint remoteEndpoint) {
    _ipsecVpnEndpoint = vpn;
    _problems = problems;
    _remoteEndpoint = remoteEndpoint;
  }

  public IpsecVpnInfo(
      @Nonnull IpsecVpn ipsecVpn,
      @Nonnull SortedSet<Problem> problems,
      @Nullable IpsecVpn remoteEnd) {
    this(
        new IpsecVpnEndpoint(ipsecVpn),
        problems,
        (remoteEnd == null) ? null : new IpsecVpnEndpoint(remoteEnd));
  }

  @Override
  public int compareTo(IpsecVpnInfo o) {
    return Comparator.comparing(IpsecVpnInfo::getIpsecVpnEndpoint).compare(this, o);
  }

  @JsonProperty(PROP_IPSEC_VPN_ENDPOINT)
  public IpsecVpnEndpoint getIpsecVpnEndpoint() {
    return _ipsecVpnEndpoint;
  }

  @JsonProperty(PROP_PROBLEMS)
  public SortedSet<Problem> getProblems() {
    return _problems;
  }

  @JsonProperty(PROP_REMOTE_ENDPOINT)
  public IpsecVpnEndpoint getRemoteEndpoint() {
    return _remoteEndpoint;
  }

  @Override
  public String toString() {
    return String.format(
        "%s problems: %s remote: %s", _ipsecVpnEndpoint, _problems, _remoteEndpoint);
  }
}
