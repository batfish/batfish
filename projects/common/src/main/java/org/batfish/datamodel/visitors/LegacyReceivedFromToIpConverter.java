package org.batfish.datamodel.visitors;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.ReceivedFrom;
import org.batfish.datamodel.ReceivedFromInterface;
import org.batfish.datamodel.ReceivedFromIp;

/** Legacy converter from {@link org.batfish.datamodel.ReceivedFrom} to old receivedFromIp. */
@ParametersAreNonnullByDefault
public final class LegacyReceivedFromToIpConverter implements ReceivedFromVisitor<Ip> {

  public static @Nonnull Ip convert(ReceivedFrom receivedFrom) {
    return INSTANCE.visit(receivedFrom);
  }

  private static final LegacyReceivedFromToIpConverter INSTANCE =
      new LegacyReceivedFromToIpConverter();

  @Override
  public Ip visitReceivedFromInterface(ReceivedFromInterface receivedFromInterface) {
    return receivedFromInterface.getLinkLocalIp();
  }

  @Override
  public Ip visitReceivedFromIp(ReceivedFromIp receivedFromIp) {
    return receivedFromIp.getIp();
  }

  @Override
  public Ip visitReceivedFromSelf() {
    return Ip.ZERO;
  }
}
