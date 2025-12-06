package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.ObjectStreamException;
import java.io.Serial;
import javax.annotation.Nonnull;
import org.batfish.datamodel.visitors.GenericIp6SpaceVisitor;

/** An {@link Ip6Space} that contains no IPv6 addresses. */
public class EmptyIp6Space extends Ip6Space {

  public static final EmptyIp6Space INSTANCE = new EmptyIp6Space();

  private EmptyIp6Space() {}

  @Override
  public <R> R accept(GenericIp6SpaceVisitor<R> visitor) {
    return visitor.visitEmptyIp6Space(this);
  }

  @Override
  protected int compareSameClass(Ip6Space o) {
    return 0;
  }

  @Override
  public Ip6Space complement() {
    return UniverseIp6Space.INSTANCE;
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
  public @Nonnull String toString() {
    return "empty";
  }

  ///////// Ensure that instances are interned.

  @JsonCreator
  private static EmptyIp6Space jsonCreator() {
    return INSTANCE;
  }

  /** Cache after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return INSTANCE;
  }
}
