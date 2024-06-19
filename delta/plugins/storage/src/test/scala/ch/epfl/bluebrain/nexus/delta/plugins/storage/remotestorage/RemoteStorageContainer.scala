package ch.epfl.bluebrain.nexus.delta.plugins.storage.remotestorage

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.{DockerImageName, MountableFile}

import java.nio.file.Path

class RemoteStorageContainer(storageVersion: String, rootVolume: Path)
    extends GenericContainer[RemoteStorageContainer](
      DockerImageName.parse(s"bluebrain/nexus-storage:$storageVersion")
    ) {

  addEnv("JAVA_OPTS", "-Xmx256m -Dconfig.override_with_env_vars=true")
  addEnv("CONFIG_FORCE_app_subject_anonymous", "true")
  addEnv("CONFIG_FORCE_app_instance_interface", "0.0.0.0")
  addEnv("CONFIG_FORCE_app_storage_root__volume", "/app")
  withCopyToContainer(MountableFile.forHostPath(rootVolume.toString, Integer.getInteger("777")), "/app")
  addExposedPort(8080)
  setWaitStrategy(Wait.forLogMessage(".*Bound\\sto.*", 1))
}
