package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.LineAction;

/** The configuration of {@code s_ipap_access_list}. */
@ParametersAreNonnullByDefault
public class IpAsPathAccessListLine implements Serializable {
  // https://www.arista.com/en/um-eos/eos-border-gateway-protocol-bgp#xx1116711
  public enum OriginType {
    ANY,
    EGP,
    IGP,
    INCOMPLETE
  }

  private final @Nonnull LineAction _action;
  private final @Nonnull OriginType _originType;
  private final @Nonnull String _regex;

  public IpAsPathAccessListLine(LineAction action, String regex, OriginType originType) {
    _action = action;
    _originType = originType;
    _regex = regex;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nonnull OriginType getOriginType() {
    return _originType;
  }

  public @Nonnull String getRegex() {
    return _regex;
  }

  public AsPathAccessListLine toAsPathAccessListLine() {
    // TODO: take origin into account here.
    String regex = AristaConfiguration.toJavaRegex(_regex);
    return new AsPathAccessListLine(_action, regex);
  }
}
