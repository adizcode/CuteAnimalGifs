package com.github.adizcode.cuteanimalgifs.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.github.adizcode.cuteanimalgifs.R
import com.github.adizcode.cuteanimalgifs.databinding.CuteAnimalGifBinding
import com.github.adizcode.cuteanimalgifs.model.CuteAnimalGif
import com.github.adizcode.cuteanimalgifs.util.Util
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily

class CuteAnimalGifsAdapter(
    private val list: MutableList<CuteAnimalGif>,
    private val requestManager: RequestManager
) : RecyclerView.Adapter<CuteAnimalGifsAdapter.ViewHolder>() {

    inner class ViewHolder(binding: CuteAnimalGifBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView: ShapeableImageView = binding.imageView

        init {
            val radius = Util.dpToPx(imageView.context, 25)
            imageView.shapeAppearanceModel = imageView.shapeAppearanceModel.toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, radius).build()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = CuteAnimalGifBinding.inflate(layoutInflater)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        requestManager
            .asGif()
            .load(list[position].url)
            .placeholder(
                R.drawable.gif_placeholder
            )
            .into(holder.imageView)

        holder.imageView.setOnClickListener {

            val shareIntent = Intent.createChooser(Intent().apply {

                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, list[position].url)

            }, null)

            holder.imageView.context.startActivity(shareIntent)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}