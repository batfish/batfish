package org.batfish.representation.cisco;

import javax.annotation.ParametersAreNonnullByDefault;

/** Visitor of {@link Track} that returns a generic value of type {@code T}. */
@ParametersAreNonnullByDefault
public interface TrackVisitor<T> {
  default T visit(Track t) {
    return t.accept(this);
  }

  T visitTrackInterface(TrackInterface trackInterface);

  T visitTrackIpSla(TrackIpSla trackIpSla);
}
