package com.example.tugasakhir.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tugasakhir.databinding.ItemDicomBinding
import com.example.tugasakhir.model.DicomData
import com.example.tugasakhir.presentation.dicom.DicomActivity

class HomeAdapter : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {
    private val files = mutableListOf<DicomData>()
    var onDeleteClicked: ((DicomData) -> Unit)? = null

    fun submitList(newList: List<DicomData>) {
        files.clear()
        files.addAll(newList)
        notifyDataSetChanged()
    }

    class HomeViewHolder(val binding: ItemDicomBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val binding = ItemDicomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val item = files[position]
        holder.binding.tvJudul.text = item.filename
//        Log.d("HomeAdapter", "Menampilkan item: ${item.filename}")

        holder.binding.root.setOnClickListener {
            val intent = Intent(it.context, DicomActivity::class.java).apply {
                putExtra("FILE_URL", item.fileUrl)
                putExtra("FILE_NAME", item.filename)
                putExtra("DICOM_ID", item.id)
                putExtra("USER_ID",item.userId)
            }
            it.context.startActivity(intent)
        }

        holder.binding.llDicom.setOnClickListener {
            onDeleteClicked?.invoke(item)
        }

    }

    override fun getItemCount() = files.size
}