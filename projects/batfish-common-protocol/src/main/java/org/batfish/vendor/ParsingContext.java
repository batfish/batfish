package org.batfish.vendor;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Wrapper class for any context needed to parse {@link VendorConfiguration} that is not included in
 * the source files for that configuration. This class does not result in any additional VI devices,
 * it only supplements parsing.
 */
public final class ParsingContext implements Serializable {
  public static final ParsingContext EMPTY_CONVERSION_CONTEXT = new ParsingContext();

  public @Nullable VendorParsingContext getSonicConfigDbs() {
    return _sonicConfigDbs;
  }

  public ParsingContext setSonicConfigDbs(@Nullable VendorParsingContext sonicConfigDbs) {
    _sonicConfigDbs = sonicConfigDbs;
    return this;
  }

  private @Nullable VendorParsingContext _sonicConfigDbs;

  public boolean isEmpty() {
    return _sonicConfigDbs == null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ParsingContext)) {
      return false;
    }
    ParsingContext that = (ParsingContext) o;
    return Objects.equals(_sonicConfigDbs, that._sonicConfigDbs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_sonicConfigDbs);
  }
}
