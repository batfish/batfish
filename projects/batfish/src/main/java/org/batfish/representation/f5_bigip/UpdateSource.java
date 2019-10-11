package org.batfish.representation.f5_bigip;

/** BGP neighbor update source setting */
public interface UpdateSource {
  <T> T accept(UpdateSourceVisitor<T> visitor);
}
