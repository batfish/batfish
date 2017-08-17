package org.batfish.storage;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.pojo.Analysis;

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
   * Retrieve a container Path
   *
   * @param containerName Container name
   * @return Path of the container
   */
  //Needs to be removed eventually
  @Override
  public Path getContainerPath(String containerName) {
    Path containerDir = _containersLocation.resolve(containerName).toAbsolutePath();
    if (!Files.exists(containerDir)) {
      throw new BatfishException("Container '" + containerName + "' not found");
    }
    return containerDir;
  }

  @Override
  public Analysis getAnalysis(String containerName, String analysisName) {
    return null;
  }

  @Override
  public Analysis createAnalysis(String containerName, String analysisName) {
    return null;
  }

  @Override public Analysis updateAnalysis(String containerName, Analysis analysis) {
    return null;
  }

  @Override public Analysis saveOrUpdateAnalysis(String containerName, Analysis analysis) {
    return null;
  }

  @Override public boolean deleteAnalysis(String containerName, String analysisName,
      boolean force) {
    return false;
  }

}
