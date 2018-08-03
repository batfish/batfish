package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface TrackMethod extends Serializable {}
