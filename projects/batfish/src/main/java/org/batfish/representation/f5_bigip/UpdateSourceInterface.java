package org.batfish.representation.f5_bigip;

import javax.annotation.Nonnull;

/** BGP neighbor update source interface setting */
public final class UpdateSourceInterface implements UpdateSource {

  public UpdateSourceInterface(String name) {
    _name = name;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @Override
  public <T> T accept(UpdateSourceVisitor<T> visitor) {
    return visitor.visitUpdateSourceInterface(this);
  }

  private @Nonnull final String _name;
}
