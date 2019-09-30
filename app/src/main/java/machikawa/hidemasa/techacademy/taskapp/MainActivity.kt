package machikawa.hidemasa.techacademy.taskapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.realm.Case
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import io.realm.RealmChangeListener
import io.realm.Sort

const val EXTRA_TASK = "machikawa.hidemasa.techacademy.taskapp.TASK"

class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private lateinit var mRealm: Realm
    private lateinit var mTaskAdapter: TaskAdapter
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm) {
            reloadListView()
        }
    }
    // 検索機能の追加に伴い
    private lateinit var searchv:SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // タスク生成プラスボタンのリスナー
        fab.setOnClickListener { view ->
            val intent = Intent(this@MainActivity,InputActivity::class.java)
            startActivity(intent)
        }

        // Realm を生成
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        // ListView の設定
        mTaskAdapter = TaskAdapter(this@MainActivity)

        // タップをした時の処理
        listView1.setOnItemClickListener { parent, view, position, id ->
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK,task.id)
            startActivity(intent)
        }

        // 長押しした時の処理
        listView1.setOnItemLongClickListener { parent, view, position, id ->
            val task = parent.adapter.getItem(position) as Task
            val builder = AlertDialog.Builder(this@MainActivity)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか?")

            builder.setPositiveButton("OK") { _,_ ->
                val result = mRealm.where(Task::class.java).equalTo("id",task.id).findAll()

                mRealm.beginTransaction()
                result.deleteAllFromRealm()
                mRealm.commitTransaction()

                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this@MainActivity,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)

                reloadListView()
            }

            builder.setNegativeButton("CANCEL",null)

            val dialog = builder.create()
            dialog.show()

            true
        }
        reloadListView()

        // 検索機能の追加に伴い //
        // サーチビューの取得
        searchv = findViewById(R.id.searchv) as SearchView
        // 検索バー常時配置
        searchv.setIconifiedByDefault(true)
        searchv.isSubmitButtonEnabled = true
        // リスナーの登録
        searchv.setOnQueryTextListener(this)
    }

    // レルムからデータ全取り
    private fun reloadListView() {
        // Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
        val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

        // 上記の結果を、TaskList としてセットする
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
        listView1.adapter = mTaskAdapter

        mTaskAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }

    // Query Text の変更時にアクティブに動作
    override fun onQueryTextChange(newText: String?): Boolean {
        // 文字が全部消されたら全タスク表示。それ以外は Query をサブミットした時と同じ動作
        if (newText?.length == 0) {
            reloadListView()
        } else {
            onQueryTextSubmit(newText)
        }
        return true
    }

    // submit ボタンを押した時にアクティブに動作.　大文字小文字は区別しない
    override fun onQueryTextSubmit(query: String?): Boolean {
        // 画面で受け取った文字列を元にカテゴリーを検索
        val taskRealmSearchResults = mRealm.where(Task::class.java).contains("categoryName",query,Case.INSENSITIVE).findAll().sort("date",Sort.DESCENDING)

        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmSearchResults)
        listView1.adapter = mTaskAdapter
        mTaskAdapter.notifyDataSetChanged()
        return false
    }
}