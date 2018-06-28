package org.batfish.datamodel;

import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

@JsonSchemaDescription("An access-control action")
public enum LineAction {
  ACCEPT,
  REJECT
}
