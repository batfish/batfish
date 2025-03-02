package org.batfish.representation.azure;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents every azure resource having name, id, and type attribute.
 */
public class Resource implements Serializable {

    private final @Nonnull String _name;
    private final @Nonnull String _id;
    private final @Nonnull String _type;

    // Batfish doesn't handle '/' in Configuration hostnames (because of serializing)
    private final String _cleanId;

    public Resource(
            @Nullable String name,
            @Nullable String id,
            @Nullable String type
    ) {
        checkArgument(name != null, "resource name must be provided");
        checkArgument(id != null, "resource id must be provided");
        checkArgument(type != null, "resource type must be provided");
        _name = name;
        _id = id;
        _type = type;
        _cleanId = convertId(id);
    }

    private static String convertId(String id){
        return id.replace('/', '_').toLowerCase();
    }

    public String getName() {
        return _name;
    }

    public String getId() {
        return _id;
    }

    public String getType() {
        return _type;
    }

    public String getCleanId() {
        return _cleanId;
    }


}
