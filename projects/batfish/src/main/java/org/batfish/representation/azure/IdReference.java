package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IdReference implements Serializable {

    private final @Nonnull String _id;

    @JsonCreator
    public IdReference(
            @JsonProperty(AzureEntities.JSON_KEY_ID) @Nullable String id
    ){
        checkArgument(id != null, "id must be provided");
        _id = id;
    }

    public String getId() {
        return _id;
    }

}
