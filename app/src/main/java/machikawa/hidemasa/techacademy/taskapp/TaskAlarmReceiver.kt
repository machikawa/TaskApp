package machikawa.hidemasa.techacademy.taskapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import io.realm.Realm

// タスク時間がきたら Local Push つうち。
class TaskAlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // api v26 以上の場合は通知チャネルを作らねばなりません。
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                "default",
                "TaskAlert",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "タスクに関連する通知をします"
            notificationManager.createNotificationChannel(channel)
        }

        // 通知の設定
        val builder = NotificationCompat.Builder(context, "default")
        builder.setSmallIcon(R.drawable.small_icon)
        builder.setLargeIcon(BitmapFactory.decodeResource(context.resources,R.drawable.large_icon))
        builder.setWhen(System.currentTimeMillis())
        builder.setDefaults(Notification.DEFAULT_ALL)
        builder.setAutoCancel(true)

        // ちゃんとタスクに沿ったNotificationをするために適したTaskの取得
        val taskId = intent!!.getIntExtra(EXTRA_TASK, -1)
        val realm  = Realm.getDefaultInstance()
        val task = realm.where(Task::class.java).equalTo("id",taskId).findFirst()

        // タスクの情報を設定する
        builder.setTicker(task!!.title)   // 5.0以降は表示されない
        builder.setContentTitle(task.title + " の開始時間です！詳しくはタップ！")
        builder.setContentText(task.contents)

        // 通知をタップしたらアプリを起動 - タスクの編集画面へいく
        var uri:Uri = Uri.parse("machiapp://taskdetail")
        var pushedIntent = Intent(Intent.ACTION_VIEW,uri)
        pushedIntent.putExtra(PUSH_TASK,taskId)
        pushedIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        val pendingIntent = PendingIntent.getActivity(context,0,pushedIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(pendingIntent)

        // 通知の表示
        notificationManager.notify(task!!.id, builder.build())
        realm.close()
    }
}