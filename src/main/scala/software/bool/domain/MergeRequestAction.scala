package software.bool.domain

enum MergeRequestAction(val label: String):
  case open extends MergeRequestAction("opened")
  case close extends MergeRequestAction("closed")
  case reopen extends MergeRequestAction("reopened")
  case update extends MergeRequestAction("updated")
  case approved extends MergeRequestAction("approved")
  case unapproved extends MergeRequestAction("unapproved")
  case approval extends MergeRequestAction("approved")
  case unapproval extends MergeRequestAction("unapproved")
  case merge extends MergeRequestAction("merged")
