package software.boolean

import zio.{UIO, ZIO}

object Notifications {

  def commentOnMergeRequest(
                             authorName: String,
                             mergeRequestId: Int,
                             commentUrl: String,
                             commentText: String,
                             projectName: String,
                           ): UIO[Unit] = ZIO.logInfo(s"${authorName} has commented on merge request ${mergeRequestId} in ${projectName}: $commentText")

  /**
   * Duarte Saraiva has commented[url] on merge request 17 in project-one (open)
   * | This MR needs improvemnt
   * */

  def mergeRequest(
                    action: String,
                    userName: String,
                    mergeRequestId: Int,
                    mergeRequestUrl: String,
                    branchName: String,
                    projectName: String,
                  ): UIO[Unit] =
    ZIO.logInfo(s"$userName $action merge request $mergeRequestId for branch $branchName in $projectName")

  /**
   * Duarte Saraiva action merge request 12[url] for branch <branchName> in project
   */
}
