import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.ims.R

const val COLLISION_CHANNEL_ID = "collision_channel"
const val MOWING_SESSION_CHANNEL_ID = "mowing_session_channel"

fun createMowingSessionNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Mowing Session Channel"
        val descriptionText = "Notification channel for mowing session events"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(MOWING_SESSION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun createCollisionNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Collision Channel"
        val descriptionText = "Notification channel for collision events"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(COLLISION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun sendCollisionNotification(context: Context, bitmap: Bitmap, imageClassification: String) {

    val notificationView = RemoteViews(context.packageName, R.layout.notification_image_layout)
    notificationView.setImageViewBitmap(R.id.notification_imageView, bitmap)

    val builder = NotificationCompat.Builder(context, COLLISION_CHANNEL_ID)
        .setSmallIcon(R.drawable.baseline_notifications_24)
        .setContentTitle("A collision has been avoided!")
        .setContentText("Expand to see why")
        .setStyle(
            NotificationCompat.BigPictureStyle()
                .bigPicture(bitmap)
                .setBigContentTitle("A collision has been avoided!")
                .setSummaryText("Due to a $imageClassification")
        )
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)


    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notify(1, builder.build())
    }
}

