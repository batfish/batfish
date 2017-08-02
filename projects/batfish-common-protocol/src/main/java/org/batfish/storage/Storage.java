package org.batfish.storage;

import java.nio.file.Path;
import java.util.stream.Stream;

/** Common storage APIs */
public interface Storage {

  Path getContainer(String containerName);

  String createContainer(String containerName);

  Stream<String> getAllContainers();
}
