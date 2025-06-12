package com.sjbt.sdk.sample.ui.photo

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.sjbt.sdk.sample.MyApplication
import com.sjbt.sdk.sample.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotoLibraryAdapter(
    private var items: List<PhotoLibraryItem>,
    private val onItemClick: (position: Int, photos: List<PhotoItem>, imageView: ImageView) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_PHOTO = 1
    }

    private var selectedPhotos: Set<PhotoItem> = emptySet()
    
    // 创建一个可重用的请求选项，避免在每次绑定时重新创建
    private val glideOptions = RequestOptions()
        .centerCrop()
//      .placeholder(R.color.img_place_holder)
        .diskCacheStrategy(DiskCacheStrategy.ALL) // 缓存所有尺寸
        .dontAnimate() // 禁用动画效果，防止闪烁

    // 更新选中的照片
    fun setSelectedPhotos(photos: Set<PhotoItem>) {
        selectedPhotos = photos
        notifyDataSetChanged() // 简单起见，全部刷新
    }
    
    // 更新单个照片的选中状态
    fun updatePhotoSelection(photo: PhotoItem) {
        // 找到照片在adapter中的位置
        val position = items.indexOfFirst { item ->
            item is PhotoLibraryItem.Photo && item.photo.id == photo.id
        }
        if (position != -1) {
            LogUtils.d("PhotoAdapter", "更新照片选中状态: 位置=$position, ID=${photo.id}")
            notifyItemChanged(position, "selection_changed")
        } else {
            LogUtils.e("PhotoAdapter", "找不到照片位置: ID=${photo.id}")
        }
    }

    // 更新数据
    fun updateItems(newItems: List<PhotoLibraryItem>) {
        items = newItems
        LogUtils.d("PhotoAdapter", "更新数据: 总数=${newItems.size}, 标题=${newItems.count { it is PhotoLibraryItem.Header }}, 照片=${newItems.count { it is PhotoLibraryItem.Photo }}")
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        val type = when (items[position]) {
            is PhotoLibraryItem.Header -> TYPE_HEADER
            is PhotoLibraryItem.Photo -> TYPE_PHOTO
        }
        return type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_photo_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_photo, parent, false)
                PhotoViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is PhotoLibraryItem.Header -> {
                LogUtils.d("PhotoAdapter", "绑定Header: 位置=$position")
                (holder as HeaderViewHolder).bind(item.date)
            }
            is PhotoLibraryItem.Photo -> {
                (holder as PhotoViewHolder).bind(item.photo, position)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty() && holder is PhotoViewHolder) {
            val item = items[position]
            if (item is PhotoLibraryItem.Photo) {
                payloads.forEach { payload ->
                    when (payload) {
                        "selection_changed" -> {
                            LogUtils.d("PhotoAdapter", "局部更新选中状态: 位置=$position")
                            holder.updateSelection(item.photo)
                            return
                        }
                    }
                }
            }
        }
        // 如果没有payload或处理失败，执行完整绑定
        onBindViewHolder(holder, position)
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val dateFormat = SimpleDateFormat("yyyy年M月d日", Locale.getDefault())

        fun bind(date: Date) {
            if(TimeUtils.isToday(date)){
                tvDate.text = MyApplication.instance.getString(R.string.toaday)
            }else{
                val formattedDate =  DateUtils.formatDateTime(
                    MyApplication.instance,
                    date.time,
                    (DateUtils.FORMAT_SHOW_DATE or
                            DateUtils.FORMAT_ABBREV_MONTH))

                LogUtils.d("PhotoAdapter", "设置Header日期: $formattedDate")
                tvDate.text = formattedDate
            }
        }
    }

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPhoto: ImageView = itemView.findViewById(R.id.ivPhoto)
        private val ivSelected: ImageView = itemView.findViewById(R.id.ivSelected)

        fun bind(photo: PhotoItem, position: Int) {
            // 设置transitionName用于共享元素动画 (在加载图片前设置，避免闪烁)
            ivPhoto.transitionName = "shared_image_${photo.id}"
            
            // 如果GlideUtil在此处不可用，则使用优化的Glide配置
            Glide.with(itemView.context)
                .load(photo.uri)
                .apply(glideOptions)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // 缓存所有尺寸
                .dontAnimate() // 禁用默认动画
                .centerCrop()
                .into(ivPhoto)

            // 处理选中状态
            updateSelection(photo)

            // 点击事件
            itemView.setOnClickListener {
                // 获取所有照片项
                val photos = items.filterIsInstance<PhotoLibraryItem.Photo>()
                    .map { it.photo }
                
                // 获取适配器位置
                val adapterPosition = bindingAdapterPosition
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                
                // 计算照片的实际位置（去除header项）
                val photoItems = items.take(adapterPosition + 1)
                    .filterIsInstance<PhotoLibraryItem.Photo>()
                
                if (photoItems.isEmpty()) return@setOnClickListener
                
                // 获取当前照片在所有照片中的索引
                val photoIndex = photos.indexOf(photo)
                
                // 启动预览，传递实际的照片索引而非适配器位置
                onItemClick(photoIndex, photos, ivPhoto)
            }
        }
        
        fun updateSelection(photo: PhotoItem) {
            val isSelected = selectedPhotos.contains(photo)
            ivSelected.visibility = if (isSelected) View.VISIBLE else View.GONE
        }
    }
}

