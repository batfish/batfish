package org.batfish.datamodel.routing_policy.expr;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Environment.Direction;

public class AutoAs extends AsExpr {

  private static final long serialVersionUID = 1L;

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return true;
  }

  @Override
  public long evaluate(Environment environment) {
    BgpProcess proc = environment.getBgpProcess();
    if (proc == null) {
      throw new BatfishException("Expected BGP process");
    }
    Direction direction = environment.getDirection();
    long as;
    Ip peerAddress = environment.getPeerAddress();
    if (peerAddress == null) {
      throw new BatfishException("Expected a peer address");
    }
    Prefix peerPrefix = Prefix.create(peerAddress, Prefix.MAX_PREFIX_LENGTH);
    // TODO: what not sure what happens with dynamic neighbors here
    BgpActivePeerConfig neighbor = proc.getActiveNeighbors().get(peerPrefix);
    if (neighbor == null) {
      throw new BatfishException("Expected a peer with address: " + peerAddress);
    }
    if (direction == Direction.IN) {
      as = neighbor.getRemoteAsns().singletonValue();
    } else if (direction == Direction.OUT) {
      as = neighbor.getLocalAs();
    } else {
      throw new BatfishException("Expected to be applied in a direction");
    }
    return as;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + 0x12345678;
    return result;
  }
}
