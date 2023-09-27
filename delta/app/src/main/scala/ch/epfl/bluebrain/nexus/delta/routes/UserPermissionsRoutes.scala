package ch.epfl.bluebrain.nexus.delta.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.epfl.bluebrain.nexus.delta.sdk.acls.AclCheck
import ch.epfl.bluebrain.nexus.delta.sdk.circe.CirceUnmarshalling
import ch.epfl.bluebrain.nexus.delta.sdk.directives.AuthDirectives
import ch.epfl.bluebrain.nexus.delta.sdk.directives.DeltaDirectives._
import ch.epfl.bluebrain.nexus.delta.sdk.identities.Identities
import ch.epfl.bluebrain.nexus.delta.sdk.model.BaseUri
import ch.epfl.bluebrain.nexus.delta.sdk.permissions.model.Permission

/**
  * The user permissions routes. Used for checking whether the current logged in user has certain permissions.
  *
  * @param identities
  *   the identities operations bundle
  * @param aclCheck
  *   verify the acls for users
  */
final class UserPermissionsRoutes(identities: Identities, aclCheck: AclCheck)(implicit
    baseUri: BaseUri
) extends AuthDirectives(identities, aclCheck)
    with CirceUnmarshalling {

  def routes: Route =
    baseUriPrefix(baseUri.prefix) {
      pathPrefix("user") {
        pathPrefix("permissions") {
          projectRef { project =>
            extractCaller { implicit caller =>
              head {
                parameter("permission".as[Permission]) { permission =>
                  authorizeFor(project, permission)(caller) {
                    complete(StatusCodes.NoContent)
                  }
                }
              }
            }
          }
        }
      }
    }
}

object UserPermissionsRoutes {
  def apply(identities: Identities, aclCheck: AclCheck)(implicit
      baseUri: BaseUri
  ): Route =
    new UserPermissionsRoutes(identities, aclCheck: AclCheck).routes
}