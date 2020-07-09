package org.batfish.representation.aws;

import static org.batfish.representation.aws.Utils.checkNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;

/** Represents an AWS prefix list */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
public final class PrefixList implements AwsVpcEntity, Serializable {

  @Nonnull private final List<Prefix> _cidrs;

  @Nonnull private final String _prefixListId;

  @Nonnull private final String _prefixListName;

  @JsonCreator
  private static PrefixList create(
      @Nullable @JsonProperty(JSON_KEY_CIDRS) List<Prefix> cidrs,
      @Nullable @JsonProperty(JSON_KEY_PREFIX_LIST_ID) String prefixListId,
      @Nullable @JsonProperty(JSON_KEY_PREFIX_LIST_NAME) String prefixListName) {
    checkNonNull(cidrs, JSON_KEY_CIDRS, "PrefixList");
    checkNonNull(prefixListId, JSON_KEY_PREFIX_LIST_ID, "PrefixList");
    checkNonNull(prefixListName, JSON_KEY_PREFIX_LIST_NAME, "PrefixList");

    return new PrefixList(prefixListId, cidrs, prefixListName);
  }

  public PrefixList(String prefixListId, List<Prefix> cidrs, String prefixListName) {
    _cidrs = cidrs;
    _prefixListId = prefixListId;
    _prefixListName = prefixListName;
  }

  @Nonnull
  public List<Prefix> getCidrs() {
    return _cidrs;
  }

  @Override
  public String getId() {
    return _prefixListId;
  }

  @Nonnull
  public String getPrefixListName() {
    return _prefixListName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PrefixList)) {
      return false;
    }
    PrefixList that = (PrefixList) o;
    return _cidrs.equals(that._cidrs)
        && Objects.equals(_prefixListId, that._prefixListId)
        && _prefixListName.equals(that._prefixListName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_cidrs, _prefixListId, _prefixListName);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_cidrs", _cidrs)
        .add("_prefixListId", _prefixListId)
        .add("_prefixListName", _prefixListName)
        .toString();
  }
}
