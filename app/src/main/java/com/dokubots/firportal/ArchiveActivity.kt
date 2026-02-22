package com.dokubots.firportal
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dokubots.firportal.databinding.ActivityArchiveBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
class ArchiveActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArchiveBinding
    private val archiveList = mutableListOf<ArchiveItem>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArchiveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.btnBack.setOnClickListener { finish() }
        loadArchive()
    }
    private fun loadArchive() {
        binding.progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder().url("${ApiClient.BASE_URL}/api/db-query").build()
                val response = ApiClient.client.newCall(request).execute()
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    val type = object : TypeToken<List<ArchiveItem>>() {}.type
                    val items: List<ArchiveItem> = Gson().fromJson(json, type)
                    withContext(Dispatchers.Main) {
                        archiveList.clear()
                        archiveList.addAll(items)
                        binding.recyclerView.adapter = ArchiveAdapter(archiveList)
                        binding.progressBar.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ArchiveActivity, "Failed to load archive", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }
    inner class ArchiveAdapter(private val items: List<ArchiveItem>) : RecyclerView.Adapter<ArchiveAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.txtTitle)
            val meta: TextView = view.findViewById(R.id.txtMeta)
            val btnDownload: View = view.findViewById(R.id.btnDownloadRecord)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.id.item_archive, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.title.text = item.filename
            holder.meta.text = "${item.ps_name} | FIR: ${item.fir_no}/${item.year} | ${item.size}"
            holder.btnDownload.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("${ApiClient.BASE_URL}/download/${item.filename}"))
                startActivity(browserIntent)
            }
        }
        override fun getItemCount() = items.size
    }
}
