package org.batfish.storage;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.batfish.common.BatfishException;
import org.batfish.common.util.CommonUtil;

public class FileStorageImpl implements Storage {

  private final Path _containersLocation;

  public FileStorageImpl(Path containersLocation) throws BatfishException {
    try {
      if (containersLocation != null) {
        _containersLocation = containersLocation;
        _containersLocation.toFile().mkdir();
      } else {
        throw new BatfishException("container location is null");
      }
    } catch (InvalidPathException e) {
      throw new BatfishException("cannot resolve containers location '" + containersLocation + "'");
    }
  }

  /**
   * Retrieve a container
   *
   * @param containerName Container name
   * @return Path of the container
   */
  @Override
  public Path getContainer(String containerName) {
    Path containerDir = _containersLocation.resolve(containerName).toAbsolutePath();
    if (!Files.exists(containerDir)) {
      throw new BatfishException("Container '" + containerName + "' not found");
    }
    return containerDir;
  }

  /**
   * Create a container
   *
   * @param containerName Create a container by this name
   * @return Name of the container
   */
  @Override
  public String createContainer(String containerName) {
    Path containerDir = _containersLocation.resolve(containerName);
    if (Files.exists(containerDir)) {
      throw new BatfishException("Container '" + containerName + "' already exists!");
    }
    if (!containerDir.toFile().mkdirs()) {
      throw new BatfishException("failed to create directory '" + containerDir.toString() + "'");
    }
    return containerName;
  }

  /**
   * Retrieve all containers as stream of container file names
   *
   * @return Steam of container file names
   */
  @Override
  public Stream<String> getAllContainers() {
    Stream<String> containersStream =
        CommonUtil.getSubdirectories(_containersLocation)
            .stream()
            .map(dir -> dir.getFileName().toString());
    //not sure whether returning a stream is a good idea, limits the use of the method
    return containersStream;
  }
}
