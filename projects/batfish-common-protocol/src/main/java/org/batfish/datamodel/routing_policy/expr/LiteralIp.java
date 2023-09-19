package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.Environment;

/** An {@link IpExpr} that is always a given literal {@link Ip}. */
@ParametersAreNonnullByDefault
public final class LiteralIp extends IpExpr {

  @Override
  public Ip evaluate(Environment env) {
    return _ip;
  }

  public static @Nonnull LiteralIp of(Ip ip) {
    return new LiteralIp(ip);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof LiteralIp)) {
      return false;
    }
    return _ip.equals(((LiteralIp) obj)._ip);
  }

  @Override
  public int hashCode() {
    return _ip.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add(PROP_IP, _ip).toString();
  }

  @JsonProperty(PROP_IP)
  public @Nonnull Ip getIp() {
    return _ip;
  }

  @JsonCreator
  private static @Nonnull LiteralIp create(@JsonProperty(PROP_IP) @Nullable Ip ip) {
    checkArgument(ip != null, "Missing %s", PROP_IP);
    return of(ip);
  }

  private LiteralIp(Ip ip) {
    _ip = ip;
  }

  private static final String PROP_IP = "ip";
  private final @Nonnull Ip _ip;
}
