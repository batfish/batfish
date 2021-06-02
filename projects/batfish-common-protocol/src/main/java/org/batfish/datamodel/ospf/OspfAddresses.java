package org.batfish.datamodel.ospf;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/**
 * Describes the {@link org.batfish.datamodel.ConcreteInterfaceAddress}es that may be used for OSPF
 * point-to-point session establishment.
 */
@ParametersAreNonnullByDefault
public class OspfAddresses implements Serializable {

  public static @Nonnull OspfAddresses of(Iterable<ConcreteInterfaceAddress> addresses) {
    return new OspfAddresses(ImmutableList.copyOf(addresses));
  }

  @JsonProperty(PROP_ADDRESSES)
  public @Nonnull List<ConcreteInterfaceAddress> getAddresses() {
    return _addresses;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof OspfAddresses)) {
      return false;
    }
    OspfAddresses that = (OspfAddresses) o;
    return _addresses.equals(that._addresses);
  }

  @Override
  public int hashCode() {
    return _addresses.hashCode();
  }

  private static final String PROP_ADDRESSES = "addresses";

  @JsonCreator
  private static @Nonnull OspfAddresses create(
      @JsonProperty(PROP_ADDRESSES) @Nullable List<ConcreteInterfaceAddress> addresses) {
    return of(ImmutableList.copyOf(firstNonNull(addresses, ImmutableList.of())));
  }

  private final @Nonnull List<ConcreteInterfaceAddress> _addresses;

  private OspfAddresses(List<ConcreteInterfaceAddress> addresses) {
    _addresses = addresses;
  }
}
