package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents a Juniper AS Path regex.
 *
 * @see <a
 *     href="https://www.juniper.net/documentation/en_US/junos/topics/reference/configuration-statement/as-path-edit-policy-options.html">{@code
 *     set policy-options as-path}</a>
 */
@ParametersAreNonnullByDefault
public class AsPath implements Serializable {

  private final String _regex;

  public AsPath(String regex) {
    _regex = regex;
  }

  @Nonnull
  public String getRegex() {
    return _regex;
  }
}
