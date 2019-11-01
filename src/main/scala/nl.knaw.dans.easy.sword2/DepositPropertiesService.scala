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

import java.io.File
import java.nio.file.attribute.FileTime

import nl.knaw.dans.easy.sword2.State.State
import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.apache.commons.configuration.PropertiesConfiguration

import scala.util.Try
import org.json4s.JsonDSL._
import org.json4s.ext.UUIDSerializer
import org.json4s._


case class State(label: String, description: String)
case class Deposit(depositId: String, creationTimeStamp: String, id: String, state: State, lastModified: String)


class DepositPropertiesService(depositId: DepositId, depositorId: Option[String] = None)(implicit depositPropertiesClient: GraphQlClient) extends DepositProperties with DebugEnhancedLogging {
  trace(depositId, depositorId)

  private val query =
    """
      |query GetDeposit($id: UUID!) {
      |  deposit(id:$id) {
      |    depositId
      |    creationTimestamp
      |    id
      |    state {
      |      label
      |      description
      |    }
      |    lastModified
      |  }
      |}
      |""".stripMargin.stripLineEnd

  /*
      deposit: {
        depositId: "...",
        creationTimestamp: "...",
        id: "...",
        state: {
          label: : "...",
          description: : "...",
        },
        lastModified: : "..."
      }
   */

  private implicit val jsonFormats: Formats = new DefaultFormats {} + UUIDSerializer
  private val result = depositPropertiesClient.doQuery(query, Map("id" -> depositId))

  result.map(_.extract[Deposit])













//  private val (properties, modified) = {
//    val props = new PropertiesConfiguration()
//  }

  override def save(): Try[Unit] = ???

  override def exists: Boolean = ???

  override def setState(state: State, descr: String): Try[DepositProperties] = ???

  override def setBagName(bagDir: File): Try[DepositProperties] = ???

  override def getState: Try[State] = ???

  override def setClientMessageContentType(contentType: String): Try[DepositProperties] = ???

  override def removeClientMessageContentType(): Try[DepositProperties] = ???

  override def getClientMessageContentType: Try[String] = ???

  override def getStateDescription: Try[String] = ???

  override def getDepositorId: Try[String] = ???

  override def getDoi: Option[String] = ???

  override def getLastModifiedTimestamp: Option[FileTime] = ???
}
