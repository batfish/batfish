package org.batfish.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import org.batfish.common.util.serialization.BatfishThirdPartySerializationModule;

public final class BatfishObjectMapper {
  private static final JsonMapper MAPPER = baseMapper().build();

  private static final JsonMapper IGNORE_UNKNOWN_MAPPER =
      baseMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build();

  private static final ObjectWriter ALWAYS_WRITER =
      baseMapper().serializationInclusion(Include.ALWAYS).build().writer();

  private static final ObjectWriter WRITER = MAPPER.writer();

  private static final PrettyPrinter PRETTY_PRINTER = new PrettyPrinter();

  private static final ObjectWriter PRETTY_WRITER =
      baseMapper().enable(SerializationFeature.INDENT_OUTPUT).build().writer(PRETTY_PRINTER);

  private static final ObjectMapper VERBOSE_MAPPER =
      baseMapper().serializationInclusion(Include.ALWAYS).build();

  private static final ObjectWriter VERBOSE_WRITER = VERBOSE_MAPPER.writer(PRETTY_PRINTER);

  /**
   * Returns a {@link ObjectMapper} configured to Batfish JSON standards. The JSON produced is not
   * pretty-printed; see {@link #prettyWriter} for that. It also doesn't include null values and
   * empty lists; see {@link #ALWAYS_WRITER} for that. If you want both features, use {@link
   * #verboseMapper()}.
   */
  public static ObjectMapper mapper() {
    return MAPPER;
  }

  /** Uses Jackson to clone the given object using the given type. */
  @VisibleForTesting // used in JSON serde tests.
  public static <T> T clone(Object o, Class<T> clazz) {
    try {
      return MAPPER.readValue(WRITER.writeValueAsBytes(o), clazz);
    } catch (IOException e) {
      throw new RuntimeException("Error cloning with Jackson", e);
    }
  }

  /** Uses Jackson to clone the given object using the given type. */
  @VisibleForTesting // used in JSON serde tests.
  public static <T> T clone(Object o, TypeReference<T> type) {
    try {
      return MAPPER.readValue(WRITER.writeValueAsBytes(o), type);
    } catch (IOException e) {
      throw new RuntimeException("Error cloning with Jackson", e);
    }
  }

  /**
   * Returns a {@link ObjectWriter} configured to Batfish JSON standards. The JSON produced is not
   * pretty-printed; see {@link #prettyWriter} for that.
   */
  public static ObjectWriter writer() {
    return WRITER;
  }

  /**
   * Returns a {@link ObjectWriter} configured to Batfish JSON standards. The JSON produced is not
   * pretty-printed; for a more concise encoding use {@link #writer}.
   */
  public static ObjectWriter prettyWriter() {
    return PRETTY_WRITER;
  }

  /**
   * Returns a {@link ObjectMapper} configured to Batfish JSON standards. Relative to {@link
   * #mapper()}, it includes null values and empty lists.
   */
  public static ObjectMapper verboseMapper() {
    return VERBOSE_MAPPER;
  }

  /**
   * Returns a {@link ObjectMapper} configured to Batfish JSON standards. Relative to {@link
   * #mapper()}, it ignores unknown properties during deserialization.
   */
  public static ObjectMapper ignoreUnknownMapper() {
    return IGNORE_UNKNOWN_MAPPER;
  }

  /**
   * Returns a {@link ObjectWriter} configured to Batfish JSON standards. The JSON produced is
   * verbosely but not pretty printed (including all fields).
   *
   * @see BatfishObjectMapper#writer()
   * @see BatfishObjectMapper#prettyWriter()
   */
  public static ObjectWriter verboseWriter() {
    return VERBOSE_WRITER;
  }

  /** Returns a JSON string representation of the given object with nulls and empties included. */
  public static String writeStringWithNulls(Object o) throws JsonProcessingException {
    return ALWAYS_WRITER.writeValueAsString(o);
  }

  /** Returns a concise JSON string representation of the given object. */
  public static String writeString(Object o) throws JsonProcessingException {
    return WRITER.writeValueAsString(o);
  }

  /** Returns a pretty JSON string representation of the given object. */
  public static String writePrettyString(Object o) throws JsonProcessingException {
    return PRETTY_WRITER.writeValueAsString(o);
  }

  /** Returns a concise JSON string representation of the given object. */
  public static String writeStringRuntimeError(Object o) {
    try {
      return writeString(o);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /** Returns a pretty JSON string representation of the given object. */
  public static String writePrettyStringRuntimeError(Object o) {
    try {
      return writePrettyString(o);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * A custom Jackson {@link DefaultPrettyPrinter} that also prints newlines between array elements,
   * which is better suited towards complex, highly-nested objects.
   */
  private static class PrettyPrinter extends DefaultPrettyPrinter {
    @Override
    public DefaultPrettyPrinter createInstance() {
      // Doc: Method called to ensure that we have a non-blueprint object to use; it is either this
      // object (if stateless), or a newly created object with separate state.
      return this;
    }

    public PrettyPrinter() {
      _arrayIndenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
    }
  }

  /** Configures all the default options for a Batfish {@link ObjectMapper}. */
  private static JsonMapper.Builder baseMapper() {
    return JsonMapper.builder()
        .disable(MapperFeature.AUTO_DETECT_CREATORS)
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        // Next two lines make Instant class serialize as an RFC-3339 timestamp
        .addModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        // This line makes Java 8's Optional type serialize
        .addModule(new Jdk8Module())
        // See https://groups.google.com/forum/#!topic/jackson-user/WfZzlt5C2Ww
        // This fixes issues in which non-empty maps with keys with empty values would get
        // omitted entirely. See also https://github.com/batfish/batfish/issues/256
        .defaultPropertyInclusion(JsonInclude.Value.construct(Include.NON_EMPTY, Include.ALWAYS))
        // This line makes Guava collections work with Jackson
        .addModule(new GuavaModule())
        // Custom (de)serialization for 3rd-party classes
        .addModule(new BatfishThirdPartySerializationModule());
  }
}
