package org.batfish.representation.palo_alto;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.representation.palo_alto.RuleEndpoint.Type;

/** PAN NAT source-translation clause (dynamic-ip-and-port) */
public class DynamicIpAndPort implements Serializable {

  private final @Nonnull List<RuleEndpoint> _translatedAddress;

  public DynamicIpAndPort() {
    _translatedAddress = new ArrayList<>();
  }

  public void addTranslatedAddress(@Nonnull RuleEndpoint ruleEndpoint) {
    checkArgument(
        ruleEndpoint.getType() != Type.Any, "Cannot use value any for translated-address");
    _translatedAddress.add(ruleEndpoint);
  }

  public @Nonnull List<RuleEndpoint> getTranslatedAddresses() {
    return ImmutableList.copyOf(_translatedAddress);
  }
}
