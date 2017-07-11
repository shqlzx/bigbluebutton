package org.bigbluebutton.core.apps.users

import org.bigbluebutton.common2.msgs._
import org.bigbluebutton.core.OutMessageGateway
import org.bigbluebutton.core.models._
import org.bigbluebutton.core.running.{ BaseMeetingActor, LiveMeeting }
import org.bigbluebutton.core2.message.senders.MsgBuilder

trait EjectUserFromMeetingCmdMsgHdlr {
  this: BaseMeetingActor =>

  val liveMeeting: LiveMeeting
  val outGW: OutMessageGateway

  def handleEjectUserFromMeetingCmdMsg(msg: EjectUserFromMeetingCmdMsg) {
    for {
      user <- Users2x.ejectFromMeeting(liveMeeting.users2x, msg.body.userId)
    } yield {
      val ejectFromMeetingEvent = MsgBuilder.buildUserEjectedFromMeetingEvtMsg(liveMeeting.props.meetingProp.intId,
        user.intId, msg.body.ejectedBy)
      outGW.send(ejectFromMeetingEvent)
      log.info("Ejecting user from meeting.  meetingId=" + ejectFromMeetingEvent.core + "  " + liveMeeting.props.meetingProp.intId + " userId=" + msg.body.userId)

      RegisteredUsers.remove(msg.body.userId, liveMeeting.registeredUsers)

      for {
        vu <- VoiceUsers.findWithIntId(liveMeeting.voiceUsers, msg.body.userId)
      } yield {
        val ejectFromVoiceEvent = MsgBuilder.buildEjectUserFromVoiceConfSysMsg(liveMeeting.props.meetingProp.intId,
          liveMeeting.props.voiceProp.voiceConf, vu.voiceUserId)
        outGW.send(ejectFromVoiceEvent)
        log.info("Ejecting user from voice.  meetingId=" + liveMeeting.props.meetingProp.intId + " userId=" + vu.intId)
      }
    }
  }

}