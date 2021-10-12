package org.batfish.vendor.a10.representation;

import org.batfish.datamodel.Ip;

/** Visitor that generates an {@link Ip} for a {@link ServerTarget}. */
public class ServerTargetToIp implements ServerTargetVisitor<Ip> {
  public static final ServerTargetToIp INSTANCE = new ServerTargetToIp();

  @Override
  public Ip visitAddress(ServerTargetAddress address) {
    return address.getAddress();
  }

  private ServerTargetToIp() {}
}
