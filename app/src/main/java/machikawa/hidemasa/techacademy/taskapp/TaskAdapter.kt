package machikawa.hidemasa.techacademy.taskapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SearchView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(context: Context): BaseAdapter() {

    private val mlayoutInfrator:LayoutInflater
    var taskList = mutableListOf<Task>()

    init {
        this.mlayoutInfrator = LayoutInflater.from(context)
    }

    // ここからはオーバーライドが必須なアイテムたち
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view:View = convertView ?: mlayoutInfrator.inflate(android.R.layout.simple_expandable_list_item_2,null)
        val textView1 = view.findViewById<TextView>(android.R.id.text1)
        val textView2 = view.findViewById<TextView>(android.R.id.text2)

        textView1.text = "タイトル: " + taskList[position].title

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPANESE)
        val date = taskList[position].date
        textView2.text = "開始日時: " + simpleDateFormat.format(date).toString()
        return view
    }

    override fun getItem(position: Int): Any {
        return taskList[position]
    }

    override fun getItemId(position: Int): Long {
        return taskList[position].id.toLong()
    }

    override fun getCount(): Int {
            return taskList.size
    }
}