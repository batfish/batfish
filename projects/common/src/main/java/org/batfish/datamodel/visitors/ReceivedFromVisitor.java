package org.batfish.datamodel.visitors;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.ReceivedFrom;
import org.batfish.datamodel.ReceivedFromInterface;
import org.batfish.datamodel.ReceivedFromIp;

/**
 * Visitor of {@link org.batfish.datamodel.ReceivedFrom} that returns a generic value of type {@code
 * T}.
 */
@ParametersAreNonnullByDefault
public interface ReceivedFromVisitor<T> {

  default T visit(ReceivedFrom receivedFrom) {
    return receivedFrom.accept(this);
  }

  T visitReceivedFromIp(ReceivedFromIp receivedFromIp);

  T visitReceivedFromInterface(ReceivedFromInterface receivedFromInterface);

  T visitReceivedFromSelf();
}
