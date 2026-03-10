package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.ObjectStreamException;
import java.io.Serial;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class UniverseIpSpace extends IpSpace {

  public static final UniverseIpSpace INSTANCE = new UniverseIpSpace();

  private UniverseIpSpace() {}

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> ipSpaceVisitor) {
    return ipSpaceVisitor.visitUniverseIpSpace(this);
  }

  @Override
  protected int compareSameClass(IpSpace o) {
    return 0;
  }

  @Override
  public IpSpace complement() {
    return EmptyIpSpace.INSTANCE;
  }

  @Override
  protected boolean exprEquals(Object o) {
    return true;
  }

  @Override
  public int hashCode() {
    return getClass().getCanonicalName().hashCode();
  }

  @Override
  public String toString() {
    return "universe";
  }

  ///////// Ensure that instances are interned.

  @JsonCreator
  private static UniverseIpSpace jsonCreator() {
    return INSTANCE;
  }

  /** Cache after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return INSTANCE;
  }
}
