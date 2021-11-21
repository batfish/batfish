package org.batfish.vendor;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * Wrapper class for any context needed to parse {@link VendorConfiguration} that is not included in
 * the source files for that configuration. This class does not result in any additional VI devices,
 * it only supplements parsing.
 */
public final class ParsingContext implements Serializable {
  public static final ParsingContext EMPTY_CONVERSION_CONTEXT = new ParsingContext();

  public @Nullable VendorSupplementalInformation getSonicConfigDbs() {
    return _sonicConfigDbs;
  }

  public void setSonicConfigDbs(@Nullable VendorSupplementalInformation sonicConfigDbs) {
    _sonicConfigDbs = sonicConfigDbs;
  }

  private @Nullable VendorSupplementalInformation _sonicConfigDbs;

  public boolean isEmpty() {
    return _sonicConfigDbs == null;
  }
}
