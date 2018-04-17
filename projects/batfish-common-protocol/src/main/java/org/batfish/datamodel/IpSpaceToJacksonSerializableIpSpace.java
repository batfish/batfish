package org.batfish.datamodel;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

public class IpSpaceToJacksonSerializableIpSpace extends JsonSerializer<IpSpace>
    implements GenericIpSpaceVisitor<JacksonSerializableIpSpace> {

  @Override
  public void serialize(IpSpace value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    gen.writeObject(value.accept(this));
  }

  @Override
  public JacksonSerializableIpSpace castToGenericIpSpaceVisitorReturnType(Object o) {
    return (JacksonSerializableIpSpace) o;
  }

  @Override
  public JacksonSerializableIpSpace visitAclIpSpace(AclIpSpace aclIpSpace) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public JacksonSerializableIpSpace visitEmptyIpSpace(EmptyIpSpace emptyIpSpace) {
    return emptyIpSpace;
  }

  @Override
  public JacksonSerializableIpSpace visitIp(Ip ip) {
    return new SimpleJsonIpSpace(ip);
  }

  @Override
  public JacksonSerializableIpSpace visitIpWildcard(IpWildcard ipWildcard) {
    return new SimpleJsonIpSpace(ipWildcard);
  }

  @Override
  public JacksonSerializableIpSpace visitIpWildcardSetIpSpace(
      IpWildcardSetIpSpace ipWildcardSetIpSpace) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public JacksonSerializableIpSpace visitPrefix(Prefix prefix) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public JacksonSerializableIpSpace visitUniverseIpSpace(UniverseIpSpace universeIpSpace) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }
}
