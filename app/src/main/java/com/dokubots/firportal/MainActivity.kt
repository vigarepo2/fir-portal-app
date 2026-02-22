package com.dokubots.firportal
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dokubots.firportal.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val stations = listOf(
        Station("Amir Khas", "25810014", "25810", "Amir-Khas"),
        Station("Arniwala", "25810005", "25810", "Arniwala"),
        Station("Bahawala", "25810010", "25810", "Bahawala"),
        Station("City 1 Abohar", "25810006", "25810", "City-1-Abohar"),
        Station("City 2 Abohar", "25810007", "25810", "City-2-Abohar"),
        Station("City Fazilka", "25810003", "25810", "City-Fazilka"),
        Station("City Jalalabad", "25810001", "25810", "City-Jalalabad"),
        Station("Khui Khera", "25525037", "25810", "Khui-Khera"),
        Station("Khuian Sarwar", "25810009", "25810", "Khuian-Sarwar"),
        Station("Sadar Abohar", "25810008", "25810", "Sadar-Abohar"),
        Station("Sadar Fazilka", "25810004", "25810", "Sadar-Fazilka"),
        Station("Sadar Jalalabad", "25810002", "25810", "Sadar-Jalalabad"),
        Station("Vairoke", "25810012", "25810", "Vairoke"),
        Station("Cyber Crime", "25810015", "25810", "Cyber-Crime-Fazilka"),
        Station("SSOC Fazilka", "25810011", "25524", "SSOC-Fazilka"),
        Station("SSOC Amritsar", "25524002", "25524", "SSOC-Amritsar"),
        Station("SSOC SAS Nagar", "25524003", "25524", "SSOC-SAS-Nagar")
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupSpinners()
        binding.btnArchive.setOnClickListener {
            startActivity(Intent(this, ArchiveActivity::class.java))
        }
        binding.btnDownload.setOnClickListener {
            val firNum = binding.inputFir.text.toString().trim()
            if (firNum.isEmpty()) {
                Toast.makeText(this, "Enter FIR Number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val station = binding.spinnerStation.selectedItem as Station
            val year = binding.spinnerYear.selectedItem.toString()
            downloadFir(station, firNum, year)
        }
    }
    private fun setupSpinners() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, stations)
        binding.spinnerStation.adapter = adapter
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear downTo 2015).toList()
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)
        binding.spinnerYear.adapter = yearAdapter
    }
    private fun downloadFir(station: Station, firNum: String, year: String) {
        binding.btnDownload.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE
        binding.btnText.visibility = View.GONE
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("psCode", station.psCode)
                    .addFormDataPart("districtCode", station.districtCode)
                    .addFormDataPart("psName", station.psName)
                    .addFormDataPart("firNumber", firNum)
                    .addFormDataPart("firYear", year)
                    .build()
                val request = Request.Builder()
                    .url("${ApiClient.BASE_URL}/api/download-fir")
                    .post(requestBody)
                    .build()
                val response = ApiClient.client.newCall(request).execute()
                if (response.isSuccessful) {
                    val bytes = response.body?.bytes() ?: throw Exception("Empty body")
                    val fileName = response.header("X-Name") ?: "FIR_${firNum}_${year}.pdf"
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(downloadsDir, fileName)
                    FileOutputStream(file).use { it.write(bytes) }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
                        openPdf(file)
                    }
                } else {
                    withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "Not found on server", Toast.LENGTH_LONG).show() }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "Network Error", Toast.LENGTH_SHORT).show() }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.btnDownload.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.btnText.visibility = View.VISIBLE
                }
            }
        }
    }
    private fun openPdf(file: File) {
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.addCompletedDownload(file.name, file.name, true, "application/pdf", file.absolutePath, file.length(), true)
    }
}
