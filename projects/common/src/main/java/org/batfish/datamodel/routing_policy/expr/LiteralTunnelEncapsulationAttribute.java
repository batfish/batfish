package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.Environment;

public class LiteralTunnelEncapsulationAttribute extends TunnelEncapsulationAttributeExpr {
  private static final String PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE = "tunnelEncapsulationAttribute";
  private final @Nonnull TunnelEncapsulationAttribute _tunnelEncapsulationAttribute;

  @JsonCreator
  private static LiteralTunnelEncapsulationAttribute jsonCreator(
      @JsonProperty(PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE) @Nullable
          TunnelEncapsulationAttribute tunnelEncapsulationAttribute) {
    checkNotNull(tunnelEncapsulationAttribute, "Missing %s", PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE);
    return new LiteralTunnelEncapsulationAttribute(tunnelEncapsulationAttribute);
  }

  public LiteralTunnelEncapsulationAttribute(
      TunnelEncapsulationAttribute tunnelEncapsulationAttribute) {
    _tunnelEncapsulationAttribute = tunnelEncapsulationAttribute;
  }

  @Override
  public TunnelEncapsulationAttribute evaluate(Environment environment) {
    return _tunnelEncapsulationAttribute;
  }

  @JsonProperty(PROP_TUNNEL_ENCAPSULATION_ATTRIBUTE)
  public @Nonnull TunnelEncapsulationAttribute getTunnelEncapsulationAttribute() {
    return _tunnelEncapsulationAttribute;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof LiteralTunnelEncapsulationAttribute)) {
      return false;
    }
    LiteralTunnelEncapsulationAttribute o = (LiteralTunnelEncapsulationAttribute) obj;
    return _tunnelEncapsulationAttribute.equals(o._tunnelEncapsulationAttribute);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_tunnelEncapsulationAttribute);
  }

  @Override
  public String toString() {
    return _tunnelEncapsulationAttribute.toString();
  }
}
