package org.batfish.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class BatfishObjectMapper extends ObjectMapper {

  /**
   * A custom Jackson {@link DefaultPrettyPrinter} that also prints newlines between array elements,
   * which is better suited towards complex, highly-nested objects.
   */
  private static class PrettyPrinter extends DefaultPrettyPrinter {
    /** */
    private static final long serialVersionUID = 1L;

    public PrettyPrinter() {
      _arrayIndenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
    }
  }

  private static final PrettyPrinter PRETTY_PRINTER = new PrettyPrinter();

  /** */
  private static final long serialVersionUID = 1L;

  public BatfishObjectMapper() {
    this(true);
  }

  public BatfishObjectMapper(boolean indent) {
    if (indent) {
      enable(SerializationFeature.INDENT_OUTPUT);
    }
    enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
    // Next two lines make Instant class serialize as an RFC-3339 timestamp
    registerModule(new JavaTimeModule());
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    // See https://groups.google.com/forum/#!topic/jackson-user/WfZzlt5C2Ww
    //  This fixes issues in which non-empty maps with keys with empty values would get omitted
    //  entirely. See also https://github.com/batfish/batfish/issues/256
    setDefaultPropertyInclusion(JsonInclude.Value.construct(Include.NON_EMPTY, Include.ALWAYS));
    setDefaultPrettyPrinter(PRETTY_PRINTER);
  }

  public BatfishObjectMapper(ClassLoader cl) {
    this();
    TypeFactory tf = TypeFactory.defaultInstance().withClassLoader(cl);
    setTypeFactory(tf);
  }
}
