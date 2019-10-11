package org.batfish.representation.f5_bigip;

public interface UpdateSourceVisitor<T> {
  T visitUpdateSourceIp(UpdateSourceIp updateSourceIp);

  T visitUpdateSourceInterface(UpdateSourceInterface updateSourceInterface);
}
