package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.common.util.ComparableStructure;

@JsonSchemaDescription("A List of static or dynamic Crypto Maps ")
public class CryptoMapSet extends ComparableStructure<String> {

  private static final String PROP_DYNAMIC = "dynamic";

  private static final String PROP_CRYPTO_MAP_ENRTIES = "cryptoMapEntries";

  private static final long serialVersionUID = 1L;

  private boolean _dynamic;
  private List<CryptoMapEntry> _cryptoMapEntries;

  @JsonCreator
  public CryptoMapSet(@JsonProperty(PROP_NAME) String name) {
    super(name);
    _cryptoMapEntries = new ArrayList<>();
  }

  @JsonProperty(PROP_DYNAMIC)
  public boolean getDynamic() {
    return _dynamic;
  }

  @JsonProperty(PROP_DYNAMIC)
  public void setDynamic(boolean dynamic) {
    _dynamic = dynamic;
  }

  @Nonnull
  @JsonProperty(PROP_CRYPTO_MAP_ENRTIES)
  public List<CryptoMapEntry> getCryptoMapEntries() {
    return _cryptoMapEntries;
  }

  @JsonProperty(PROP_CRYPTO_MAP_ENRTIES)
  public void setCryptoMapEntries(@Nonnull List<CryptoMapEntry> cryptoMapEntries) {
    _cryptoMapEntries = cryptoMapEntries;
  }
}
