package org.batfish.common.util.serialization.guava;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.util.Converter;
import com.google.common.collect.RangeSet;
import javax.annotation.Nonnull;

/** Custom deserializer for {@link RangeSet} */
public final class RangeSetDeserializer extends StdDelegatingDeserializer<RangeSet<?>>
    implements ContextualDeserializer {

  public RangeSetDeserializer(@Nonnull JavaType type) {
    super(new RangeSetDeserializerConverter(type));
  }

  @Override
  protected @Nonnull StdDelegatingDeserializer<RangeSet<?>> withDelegate(
      Converter<Object, RangeSet<?>> converter,
      JavaType delegateType,
      JsonDeserializer<?> delegateDeserializer) {
    return new StdDelegatingDeserializer<>(converter, delegateType, delegateDeserializer);
  }
}
