package expo.modules.backgroundnotificationhandler

import android.content.Context
import com.google.firebase.messaging.RemoteMessage

class BackgroundNotificationHandler(
  private val context: Context,
  private val notifInterface: BackgroundNotificationHandlerInterface,
) {
  fun handleMessage(remoteMessage: RemoteMessage) {
    if (ExpoBackgroundNotificationHandlerModule.isForegrounded) {
      return
    }

    // Get the reason and decide the channel
    val reason = remoteMessage.data["reason"]
    
    // Create a MUTABLE copy of the data map
    val modifiedData = remoteMessage.data.toMutableMap()

    if (reason == "chat-message" || reason == "chat-reaction") {
        applyChatLogic(modifiedData)
    } else {
        applyOtherLogic(reason, modifiedData)
    }

    // Wrap the modified data back into a form the interface accepts
    notifInterface.showMessage(remoteMessage, modifiedData) 
  }

  private fun applyChatLogic(data: MutableMap<String, String>) {
    val playSound = NotificationPrefs(context).getBoolean("playSoundChat")
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      data["channelId"] = if (playSound) "chat-messages" else "chat-messages-muted"
    } else {
      data["sound"] = if (playSound) "dm.mp3" else null
    }
  }

  private fun applyOtherLogic(reason: String?, data: MutableMap<String, String>) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      val validReasons = listOf("like", "repost", "follow", "mention", "reply", "quote", "like-via-repost", "repost-via-repost", "subscribed-post")
      if (validReasons.contains(reason)) {
        // Map the 'reason' string directly to the Channel ID
        data["channelId"] = reason!!
      }
    }
  }
}
