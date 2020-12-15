package ch.epfl.bluebrain.nexus.delta.plugins.storage.serialization

import ch.epfl.bluebrain.nexus.delta.plugins.storage.storages.model.EncryptionState.Encrypted
import ch.epfl.bluebrain.nexus.delta.plugins.storage.storages.model.{DigestAlgorithm, Secret, StorageEvent}
import ch.epfl.bluebrain.nexus.delta.plugins.storage.storages.model.StorageEvent.{StorageCreated, StorageDeprecated, StorageTagAdded, StorageUpdated}
import ch.epfl.bluebrain.nexus.delta.plugins.storage.storages.model.StorageValue.{DiskStorageValue, RemoteDiskStorageValue, S3StorageValue}
import ch.epfl.bluebrain.nexus.delta.rdf.Vocabulary.nxv
import ch.epfl.bluebrain.nexus.delta.sdk.model.Label
import ch.epfl.bluebrain.nexus.delta.sdk.model.identities.Identity.{Subject, User}
import ch.epfl.bluebrain.nexus.delta.sdk.model.permissions.Permission
import ch.epfl.bluebrain.nexus.delta.sdk.model.projects.ProjectRef
import ch.epfl.bluebrain.nexus.testkit.{CirceLiteral, TestHelpers}
import io.circe.Json
import org.scalatest.CancelAfterFailure
import org.scalatest.flatspec.AnyFlatSpecLike

import java.nio.file.Paths
import java.time.Instant
import scala.collection.immutable.VectorMap

class EventSerializerSpec
    extends EventSerializerBehaviours
    with AnyFlatSpecLike
    with TestHelpers
    with CirceLiteral
    with CancelAfterFailure {

  private val instant: Instant = Instant.EPOCH
  private val subject: Subject = User("username", Label.unsafe("myrealm"))
  private val org              = Label.unsafe("myorg")
  private val proj             = Label.unsafe("myproj")
  private val tag              = Label.unsafe("mytag")
  private val dId              = nxv + "disk-storage"
  private val s3Id             = nxv + "s3-storage"
  private val rdId             = nxv + "remote-disk-storage"
  private val projectRef       = ProjectRef(org, proj)
  private val alg              = DigestAlgorithm.default

  // format: off
  private val diskVal         = DiskStorageValue(default = true, alg, Paths.get("/tmp"), Permission.unsafe("disk/read"), Permission.unsafe("disk/write"), 50, Encrypted)
  private val diskValUpdate   = DiskStorageValue(default = false, alg, Paths.get("/tmp"), Permission.unsafe("disk/read"), Permission.unsafe("disk/write"), 40, Encrypted)
  private val s3Val           = S3StorageValue(default = true, alg, "mybucket", Some("http://localhost"), Some(Secret.encrypted("accessKey")), Some(Secret.encrypted("secretKey")), None, Permission.unsafe("s3/read"), Permission.unsafe("s3/write"), 51, Encrypted)
  private val s3ValUpdate     = S3StorageValue(default = true, alg, "mybucket2", Some("http://localhost"), Some(Secret.encrypted("accessKey")), Some(Secret.encrypted("secretKey")), None, Permission.unsafe("s3/read"), Permission.unsafe("s3/write"), 41, Encrypted)
  private val remoteVal       = RemoteDiskStorageValue(default = true, "http://localhost", Some(Secret.encrypted("authToken")), Label.unsafe("myfolder"), Permission.unsafe("remote/read"), Permission.unsafe("remote/write"), 52, Encrypted)
  private val remoteValUpdate = RemoteDiskStorageValue(default = true, "http://localhost", Some(Secret.encrypted("authToken")), Label.unsafe("myfolder2"), Permission.unsafe("remote/read"), Permission.unsafe("remote/write"), 42, Encrypted)

  val storagesMapping: Map[StorageEvent, Json] = VectorMap(
    StorageCreated(dId, projectRef, diskVal, Secret.encrypted(json"""{"disk": "created"}"""), 1, instant, subject)             -> jsonContentOf("/storage/serialization/disk-storage-created.json"),
    StorageCreated(s3Id, projectRef, s3Val, Secret.encrypted(json"""{"s3": "created"}"""), 1, instant, subject)                -> jsonContentOf("/storage/serialization/s3-storage-created.json"),
    StorageCreated(rdId, projectRef, remoteVal, Secret.encrypted(json"""{"remote": "created"}"""), 1, instant, subject)        -> jsonContentOf("/storage/serialization/remote-storage-created.json"),
    StorageUpdated(dId, projectRef, diskValUpdate, Secret.encrypted(json"""{"disk": "updated"}"""), 2, instant, subject)       -> jsonContentOf("/storage/serialization/disk-storage-updated.json"),
    StorageUpdated(s3Id, projectRef, s3ValUpdate, Secret.encrypted(json"""{"s3": "updated"}"""), 2, instant, subject)          -> jsonContentOf("/storage/serialization/s3-storage-updated.json"),
    StorageUpdated(rdId, projectRef, remoteValUpdate, Secret.encrypted(json"""{"remote": "updated"}"""), 2, instant, subject)  -> jsonContentOf("/storage/serialization/remote-storage-updated.json"),
    StorageTagAdded(dId, projectRef, targetRev = 1, tag, 3, instant, subject)                                                         -> jsonContentOf("/storage/serialization/storage-tag-added.json"),
    StorageDeprecated(dId, projectRef, 4, instant, subject)                                                                           -> jsonContentOf("/storage/serialization/storage-deprecated.json")
  )
  // format: on

  "An EventSerializer" should behave like eventToJsonSerializer("storage", storagesMapping)
  "An EventSerializer" should behave like jsonToEventDeserializer("storage", storagesMapping)

}