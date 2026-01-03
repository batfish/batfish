package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.ObjectStreamException;
import java.io.Serial;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class EmptyIpSpace extends IpSpace {

  public static final EmptyIpSpace INSTANCE = new EmptyIpSpace();

  private EmptyIpSpace() {}

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> visitor) {
    return visitor.visitEmptyIpSpace(this);
  }

  @Override
  protected int compareSameClass(IpSpace o) {
    return 0;
  }

  @Override
  public IpSpace complement() {
    return UniverseIpSpace.INSTANCE;
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
    return "empty";
  }

  ///////// Ensure that instances are interned.

  @JsonCreator
  private static EmptyIpSpace jsonCreator() {
    return INSTANCE;
  }

  /** Cache after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return INSTANCE;
  }
}
