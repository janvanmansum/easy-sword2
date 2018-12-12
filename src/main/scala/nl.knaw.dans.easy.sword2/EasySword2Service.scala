/**
 * Copyright (C) 2015 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.sword2

import io.undertow.{ Handlers, Undertow }
import io.undertow.servlet.Servlets
import nl.knaw.dans.easy.sword2.servlets._
import nl.knaw.dans.lib.logging.DebugEnhancedLogging

import scala.util.Try

class EasySword2Service(val serverPort: Int, app: EasySword2App) extends DebugEnhancedLogging {

  import logger._

  // TODO: Refactor this so that we do not need access to the application's wiring from outside the object.
  val settings = Settings(
    depositRootDir = app.wiring.depositRootDir,
    depositPermissions = app.wiring.depositPermissions,
    tempDir = app.wiring.tempDir,
    serviceBaseUrl = app.wiring.baseUrl,
    collectionPath = app.wiring.collectionPath,
    auth = app.wiring.auth,
    urlPattern = app.wiring.urlPattern,
    bagStoreSettings = app.wiring.bagStoreSettings,
    supportMailAddress = app.wiring.supportMailAddress,
    marginDiskSpace = app.wiring.marginDiskSpace,
    sample = app.wiring.sampleSettings,
    cleanup = app.wiring.cleanup,
  )

  private val deployment = Servlets.deployment()
    .setDeploymentName("App")
    .setClassLoader(classOf[EasySword2Service].getClassLoader)
    .setContextPath("/")
    .addInitParameter("config-impl", classOf[SwordConfig].getName)
    .addInitParameter("service-document-impl", classOf[ServiceDocumentManagerImpl].getName)
    .addInitParameter("collection-deposit-impl", classOf[CollectionDepositManagerImpl].getName)
    .addInitParameter("collection-list-impl", classOf[CollectionListManagerImpl].getName)
    .addInitParameter("container-impl", classOf[ContainerManagerImpl].getName)
    .addInitParameter("media-resource-impl", classOf[MediaResourceManagerImpl].getName)
    .addInitParameter("statement-impl", classOf[StatementManagerImpl].getName)
    .addServletContextAttribute(servlets.EASY_SWORD2_SETTINGS_ATTRIBUTE_KEY, settings)
    .addServlet(Servlets.servlet("Hello", classOf[HelloServlet]).addMapping("/hello"))
    .addServlet(Servlets.servlet("ServiceDocument", classOf[ServiceDocumentServletImpl]).addMapping("/servicedocument"))
    .addServlet(Servlets.servlet("Collection", classOf[CollectionServletImpl]).addMapping("/collection/*"))
    .addServlet(Servlets.servlet("Container", classOf[ContainerServletImpl]).addMapping("/container/*"))
    .addServlet(Servlets.servlet("Media", classOf[MediaResourceServletImpl]).addMapping("/media/*"))
    .addServlet(Servlets.servlet("Statement", classOf[StatementServletImpl]).addMapping("/statement/*"))

  private val manager = Servlets.defaultContainer().addDeployment(deployment)
  manager.deploy()
  private val pathHandler = Handlers.path().addPrefixPath("/", manager.start())
  private val server = Undertow.builder()
    .addHttpListener(serverPort, "0.0.0.0")
    .setHandler(pathHandler)
    .build()

  info(s"HTTP port is $serverPort")

  def start(): Try[Unit] = Try {
    debug("Starting deposit processing thread...")
    DepositHandler.startDepositProcessingStream(settings)
    info("Starting HTTP service...")
    server.start()
  }

  def stop(): Try[Unit] = Try {
    info("Stopping HTTP service...")
    server.stop()
    // TODO: stop the deposit processing thread before closing
  }

  def destroy(): Try[Unit] = Try {
    app.close()
  }
}
