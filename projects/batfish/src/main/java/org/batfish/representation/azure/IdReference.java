package org.batfish.representation.azure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IdReference implements Serializable {

    private final String _id;

    @JsonCreator
    public IdReference(
            @JsonProperty(AzureEntities.JSON_KEY_ID) String id
    ){
        _id = id;
    }

    public String getId() {
        return _id;
    }

}
