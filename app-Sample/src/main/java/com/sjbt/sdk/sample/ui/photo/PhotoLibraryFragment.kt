package com.sjbt.sdk.sample.ui.photo

import androidx.recyclerview.widget.GridLayoutManager
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.os.Build
import java.io.File
import android.os.Bundle
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Rect
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.base.api.UNIWatchMate
import com.blankj.utilcode.util.LogUtils
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentPhotoLibraryBinding
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import java.util.Date

class PhotoLibraryFragment : BaseFragment(R.layout.fragment_photo_library) {
    private val viewBind: FragmentPhotoLibraryBinding by viewBinding()

    // 恢复使用Fragment级别的ViewModel
    private val viewModel by viewModels<PhotoLibraryViewModel>()

    
    private lateinit var photoAdapter: PhotoLibraryAdapter

    // 当前预览的照片位置
    private var currentPreviewPosition = 0
    
    // 共享元素的目标ID
    private var sharedElementTargetId = ""
    private var lastSelectedPhotos: Set<PhotoItem> = emptySet()
    
    // 根据ID查找照片项
    private fun findPhotoItemById(id: String): PhotoItem? {
        return viewModel.photoItems.value
            .filterIsInstance<PhotoLibraryItem.Photo>()
            .map { it.photo }
            .find { it.id == id }
    }
    
    // 获取照片在适配器中的位置
    private fun getPhotoAdapterPosition(photoItem: PhotoItem): Int {
        val items = viewModel.photoItems.value
        for (i in items.indices) {
            if (items[i] is PhotoLibraryItem.Photo && (items[i] as PhotoLibraryItem.Photo).photo.id == photoItem.id) {
                return i
            }
        }
        return -1
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()
        initData()
    }
     fun initView() {
        // 设置标题栏按钮点击事件
        viewBind.ivCheck.setOnClickListener {
            if (viewModel.importingPhoto.value) {
                ToastUtil.showToast(getString(R.string.importing_dot))
                return@setOnClickListener
            }else{

            }
            viewModel.toggleSelectionMode()
        }

         val isNoStorageDevice = UNIWatchMate.getGlassesFunctionSupportState().noStorageDevice == 1
         viewBind.tvImport.visibility = if (isNoStorageDevice) View.GONE else View.VISIBLE
        // 导入按钮点击事件
        viewBind.tvImport.setOnClickListener {
            if (viewModel.importingPhoto.value) {
                // 如果正在导入，点击则终止导入
                viewModel.stopImport(manual = true)
            } else {
                // 开始导入
                viewModel.startImport()
            }
        }

        viewBind.ivDelete.setOnClickListener {
            viewModel.deleteSelectedPhotos()
        }
        
        // 初始化RecyclerView和Adapter
        photoAdapter = PhotoLibraryAdapter(emptyList()) { position, photos, imageView ->
            if (!viewModel.isSelectionMode.value) {
                // 普通模式下点击预览
                ToastUtil.showToast("进入预览界面")
            } else {
                // 选择模式下选中/取消选中
                val photo = photos[position]
                viewModel.togglePhotoSelection(photo)
                // 直接通过adapter更新选中状态，而不是缓存position
                photoAdapter.updatePhotoSelection(photo)
            }
        }

        // 设置GridLayoutManager，支持跨列显示header
        val layoutManager = GridLayoutManager(context, 3)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val type = photoAdapter.getItemViewType(position)
                val spanSize = when (type) {
                    PhotoLibraryAdapter.TYPE_HEADER -> 3 // header占据整行
                    else -> 1 // 图片项占据1列
                }
//                LogUtils.d("SpanSizeLookup", "位置: $position, 类型: $type, 跨度: $spanSize")
                return spanSize
            }
        }

        viewBind.recyclerView.apply {
            this.layoutManager = layoutManager
            adapter = photoAdapter
            // 添加间距装饰器，确保视觉效果更好
            if (itemDecorationCount == 0) {
                addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                        val position = parent.getChildAdapterPosition(view)
                        if (position >= 0) {
                            when (photoAdapter.getItemViewType(position)) {
                                PhotoLibraryAdapter.TYPE_HEADER -> {
                                    // 标题项的间距
                                    outRect.top = resources.getDimensionPixelSize(R.dimen.dp_2)
                                    outRect.bottom = resources.getDimensionPixelSize(R.dimen.dp_2)
                                }
                                else -> {
                                    // 照片项的间距
                                    outRect.left = resources.getDimensionPixelSize(R.dimen.dp_1)
                                    outRect.right = resources.getDimensionPixelSize(R.dimen.dp_1)
                                    outRect.bottom = resources.getDimensionPixelSize(R.dimen.dp_2)
                                }
                            }
                        }
                    }
                })
            }
        }

        // 设置空视图
        updateEmptyView()
    }

    private fun updateEmptyView() {
        viewBind.clEmptyView.visibility = if (photoAdapter.itemCount == 0) View.VISIBLE else View.GONE
    }

     fun initData() {
        // 加载图片
        viewModel.loadPhotos()
    }

     fun initEvent() {


        // 观察ViewModel状态
        viewLifecycleOwner.lifecycleScope.launch {
            // 收集Photo数据变化
            viewModel.photoItems.collectLatest { items ->
                photoAdapter.updateItems(items)
                viewBind.ivCheck.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
                updateEmptyView()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 收集importingIndex数据变化
            viewModel.photoItems.collectLatest { itms ->
                val totalPhotoCount = viewModel.photoNameList.value.size
                viewBind.tvImportingCount.text = getString(R.string.imporing_tip, viewModel.importingIndex.value, totalPhotoCount)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 收集importingIndex数据变化
            viewModel.importingIndex.collectLatest { importIndex ->
                viewBind.tvImportingCount.text = getString(R.string.imporing_tip, importIndex, viewModel.photoNameList.value.size)
                updateImportingUI(viewModel.importingPhoto.value)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 收集选择模式变化

            viewModel.isSelectionMode.collectLatest { isSelectionMode ->
                viewBind.ivCheck.setImageResource(
                    if (isSelectionMode) R.mipmap.ic_check_green_34 else R.mipmap.ic_check_34
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 收集选择的照片变化
                viewModel.selectedPhotos.collect { selectedPhotos ->
                    if (selectedPhotos != lastSelectedPhotos) {
                        lastSelectedPhotos = selectedPhotos
                        photoAdapter.setSelectedPhotos(selectedPhotos)
                        viewBind.bottomBar.visibility =
                            if (selectedPhotos.isNotEmpty()) View.VISIBLE else View.GONE
                        val maxSelectCount = viewModel.photoItems.value.count { item ->
                            item is PhotoLibraryItem.Photo
                        }
                        viewBind.tvSelectedCount.text =
                            getString(R.string.choosed_number, selectedPhotos.size, maxSelectCount)

                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 收集导入状态变化
            viewModel.importingPhoto.collectLatest { isImporting ->
                updateImportingUI(isImporting)
                if (isImporting) {
                    viewBind.tvImportingCount.text = getString(R.string.imporing_tip, viewModel.importingIndex.value, viewModel.photoNameList.value.size)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // 收集UI状态变化
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    // 更新UI状态
                    if (state.isLoading) {
                        promptProgress.showProgress(R.string.tip_please_wait)
                    } else {
                        promptProgress.dismiss()
                    }
                }
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            // 收集ViewModel发出的事件
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is PhotoLibraryEvent.StartSendPhoto -> {
                            // 处理开始发送照片事件
//                        ToastUtil.showToast("开始获取设备照片")
                        }

                        is PhotoLibraryEvent.PhotoSendProgress -> {
//                        viewBind.tvImportingCount.text = "导入中 (${event.progress}/${viewModel.uiState.value.newPhotoCount})..."
                        }

                        is PhotoLibraryEvent.PhotoSendComplete -> {
                            // 处理照片发送完成

                        }

                        is PhotoLibraryEvent.PhotoReceived -> {
                            // 处理收到照片
//                        ToastUtil.showToast("收到新照片")
                        }

                        is PhotoLibraryEvent.PhotoNamesReceived -> {
                            // 处理收到照片名称列表
                            LogUtils.e("照片库", "收到设备照片名称列表: ${event.names.size}个")
                        }

                        is PhotoLibraryEvent.DeviceNotConnected -> {
                            ToastUtil.showToast(getString(R.string.device_disconnect))
                        }

                    }
                }
            }
        }


    }
    
    private fun updateImportingUI(isImporting: Boolean) {
        if (isImporting) {
            // 显示导入中状态
            viewBind.ivInfo.visibility = View.GONE
            viewBind.tvNewPhotoCount.visibility = View.GONE

            viewBind.progressLoading.visibility = View.VISIBLE

            viewBind.tvImportingCount.visibility = View.VISIBLE
            viewBind.tvTip.visibility = View.VISIBLE
            viewBind.tvImport.apply {
                text = getString(R.string.stop_import)
                setTextColor(resources.getColor(R.color.black))
            }
        } else {
            // 恢复普通状态
            viewBind.progressLoading.clearAnimation()  // 停止动画
            // 同时停止可能运行的属性动画
            viewBind.progressLoading.animate().cancel()
            viewBind.progressLoading.visibility = View.INVISIBLE
            viewBind.tvImportingCount.visibility = View.GONE
            viewBind.tvTip.visibility = View.GONE

            viewBind.ivInfo.visibility = View.VISIBLE
            viewBind.tvNewPhotoCount.visibility = View.VISIBLE
            viewBind.tvImport.apply {
                text = getString(R.string.import_title)
                setTextColor(resources.getColor(R.color.black))
            }
            viewBind.tvImportingCount.postInvalidate();
        }
    }


    override fun onResume() {
        super.onResume()
        if (viewModel.photoItems.value.isEmpty()) {//切换到相册时，如果本地没有图片，则重新加载
            realResume()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            if (viewModel.photoItems.value.isEmpty()) {//切换到相册时，如果本地没有图片，则重新加载
                realResume()
            }
        }
    }

    private fun realResume() {
        if (viewModel.isSelectionMode.value) {
            viewModel.toggleSelectionMode()
        }
        viewModel.loadPhotos() // 每次显示时重新加载图片
    }

    // 滚动到指定照片位置
    private fun scrollToPhotoPosition(photoPosition: Int) {
        try {
            // 获取所有照片项
            val photos = viewModel.photoItems.value.filterIsInstance<PhotoLibraryItem.Photo>()
                .map { it.photo }
                
            if (photoPosition < 0 || photoPosition >= photos.size) return
                
            // 找到对应的照片
            val targetPhoto = photos[photoPosition]
            
            // 计算在RecyclerView中的实际位置（包含Header）
            viewModel.photoItems.value.forEachIndexed { index, item ->
                if (item is PhotoLibraryItem.Photo && item.photo.id == targetPhoto.id) {
                    // 检查该位置的视图是否已经完全可见
                    val layoutManager = viewBind.recyclerView.layoutManager
                    val viewHolder = viewBind.recyclerView.findViewHolderForAdapterPosition(index)

                    // 如果找到视图，检查其可见性
                    if (viewHolder != null) {
                        // 检查视图是否完全可见
                        if (!isViewFullyVisible(viewHolder.itemView)) {
                            // 只有在视图不完全可见时才滚动
                            viewBind.recyclerView.scrollToPosition(index)
                            LogUtils.d("PhotoLibrary", "滚动到位置: $index (视图不完全可见)")
                        } else {
                            LogUtils.d("PhotoLibrary", "位置: $index 已完全可见，无需滚动")
                        }
                    } else {
                        // 视图尚未创建，需要滚动到该位置
                        viewBind.recyclerView.scrollToPosition(index)
                        LogUtils.d("PhotoLibrary", "滚动到位置: $index (视图尚未创建)")
                    }
                    return
                }
            }
        } catch (e: Exception) {
            LogUtils.e("PhotoLibrary", "滚动失败: ${e.message}")
        }
    }
    
    /**
     * 检查视图是否完全在RecyclerView的可见区域内
     */
    private fun isViewFullyVisible(view: View): Boolean {
        val recyclerView = viewBind.recyclerView
        val layoutManager = recyclerView.layoutManager ?: return false

        // 获取 ViewHolder
        val viewHolder = recyclerView.getChildViewHolder(view) ?: return false
        val position = viewHolder.adapterPosition

        if (position == RecyclerView.NO_POSITION) return false

        // 使用 LayoutManager 的方法检查可见性
        return when (layoutManager) {
            is LinearLayoutManager -> {
                val firstVisible = layoutManager.findFirstCompletelyVisibleItemPosition()
                val lastVisible = layoutManager.findLastCompletelyVisibleItemPosition()
                position in firstVisible..lastVisible
            }
            is GridLayoutManager -> {
                val firstVisible = layoutManager.findFirstCompletelyVisibleItemPosition()
                val lastVisible = layoutManager.findLastCompletelyVisibleItemPosition()
                position in firstVisible..lastVisible
            }
            else -> {
                // 对于其他 LayoutManager，使用通用方法
                val rvRect = Rect(
                    recyclerView.paddingLeft,
                    recyclerView.paddingTop,
                    recyclerView.width - recyclerView.paddingRight,
                    recyclerView.height - recyclerView.paddingBottom
                )

                val viewRect = Rect()
                recyclerView.offsetDescendantRectToMyCoords(view, viewRect)
                rvRect.contains(viewRect)
            }
        }
    }

    // 添加供Activity使用的公共方法，设置从预览返回的信息
    fun setSharedElementData(position: Int, photoId: String) {
        LogUtils.d("PhotoLibrary", "设置共享元素数据: 位置=$position, ID=$photoId")
        currentPreviewPosition = position
        sharedElementTargetId = photoId
        scrollToPhotoPosition(position)
    }

    // 强制刷新共享元素映射
    fun refreshSharedElement() {
        LogUtils.d("PhotoLibrary", "刷新共享元素映射")
        if (sharedElementTargetId.isEmpty()) return
        
        val photoItem = findPhotoItemById(sharedElementTargetId) ?: return
        val position = getPhotoAdapterPosition(photoItem)
        
        if (position != -1) {
            // 确保列表滚动到该位置，并且完全可见
            viewBind.recyclerView.stopScroll() // 先停止任何正在进行的滚动

            // 检查目标位置是否已经可见
            val layoutManager = viewBind.recyclerView.layoutManager as? GridLayoutManager
            val firstVisible = layoutManager?.findFirstCompletelyVisibleItemPosition() ?: -1
            val lastVisible = layoutManager?.findLastCompletelyVisibleItemPosition() ?: -1

            if (position in firstVisible..lastVisible) {
                // 位置已经可见，直接设置共享元素
                LogUtils.d("PhotoLibrary", "目标位置已可见: $position")
                setupSharedElementDirectly(position, photoItem)
            } else {
                // 需要滚动到目标位置
                LogUtils.d("PhotoLibrary", "需要滚动到位置: $position (当前可见范围: $firstVisible-$lastVisible)")

                // 使用scrollToPosition立即滚动，而不是smoothScrollToPosition
                viewBind.recyclerView.scrollToPosition(position)

                // 等待布局完成后再尝试找到共享元素
                viewBind.recyclerView.post {
                    setupSharedElementDirectly(position, photoItem)
                }
            }
        } else {
            LogUtils.e("PhotoLibrary", "找不到照片位置: ID=$sharedElementTargetId")
        }
    }

    // 直接设置共享元素，减少等待时间
    private fun setupSharedElementDirectly(position: Int, photoItem: PhotoItem) {
        var attempts = 0
        val maxAttempts = 3

        fun trySetupSharedElement() {
            attempts++
            val viewHolder = viewBind.recyclerView.findViewHolderForAdapterPosition(position)

            if (viewHolder != null) {
                setupSharedElement(viewHolder.itemView, position, photoItem)
                LogUtils.d("PhotoLibrary", "成功设置共享元素，尝试次数: $attempts")
            } else if (attempts < maxAttempts) {
                LogUtils.d("PhotoLibrary", "第${attempts}次未找到ViewHolder，继续尝试")
                viewBind.recyclerView.post { trySetupSharedElement() }
            } else {
                LogUtils.e("PhotoLibrary", "达到最大尝试次数，无法找到ViewHolder")
            }
        }

        trySetupSharedElement()
    }

    // 在找到共享元素后设置它
    private fun setupSharedElement(itemView: View, position: Int, photoItem: PhotoItem) {
        val imageView = itemView.findViewById<ImageView>(R.id.ivPhoto)

        if (imageView != null) {
            val transitionName = "shared_image_${photoItem.id}"
            imageView.transitionName = transitionName
            LogUtils.d("PhotoLibrary", "设置共享元素: $transitionName -> 位置=$position")

            // 通知系统共享元素已更新
            activity?.let { act ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // 我们不再使用这种方式，因为MainActivity会直接调用mapSharedElement
                    // 这里只保留代码以备不时之需
                    LogUtils.d("PhotoLibrary", "已准备好共享元素，等待MainActivity调用映射方法")
                }
            }
        } else {
            LogUtils.e("PhotoLibrary", "找不到ImageView: 位置=$position")
        }
    }

    /**
     * 映射共享元素 - 由Activity调用
     *
     * @param names 共享元素名称列表
     * @return 共享元素映射（名称 -> 视图）
     */
    fun mapSharedElement(names: List<String>): Map<String, View>? {

        if (names.isEmpty()) {
            LogUtils.d("PhotoLibrary", "mapSharedElement提前返回: 名称为空或目标ID为空")
            return null
        }
        val realShareName = names[0]
        sharedElementTargetId = realShareName.replace("shared_image_","")
        LogUtils.d("PhotoLibrary", "mapSharedElement被调用: names=$names, 目标ID=$sharedElementTargetId")

        // 确保Fragment已经添加到Activity
        if (!isAdded || view == null) {
            LogUtils.e("PhotoLibrary", "Fragment未添加或视图为空")
            return null
        }

        // 查找照片项
        val photoItem = findPhotoItemById(sharedElementTargetId)
        if (photoItem == null) {
            LogUtils.e("PhotoLibrary", "找不到照片项: ID=$sharedElementTargetId")
            return null
        }

        // 获取位置
        val photoPosition = getPhotoAdapterPosition(photoItem)
        if (photoPosition == -1) {
            LogUtils.e("PhotoLibrary", "找不到照片位置: ID=$sharedElementTargetId")
            return null
        }

        LogUtils.d("PhotoLibrary", "找到照片位置: $photoPosition")

        // 确保位置可见
//        scrollToPosition(photoPosition)

        // 查找视图
        val viewHolder = viewBind.recyclerView.findViewHolderForAdapterPosition(photoPosition)
        val imageView = viewHolder?.itemView?.findViewById<ImageView>(R.id.ivPhoto)
        
        if (imageView == null) {
            LogUtils.e("PhotoLibrary", "找不到ImageView: 位置=$photoPosition")
            return null
        }
        
        // 设置transitionName
        val transitionName = "shared_image_$sharedElementTargetId"
        imageView.transitionName = transitionName
        
        // 创建映射
        LogUtils.d("PhotoLibrary", "成功映射共享元素: $transitionName -> 位置=$photoPosition")
        return mapOf(transitionName to imageView)
    }

    /**
     * 处理照片删除事件
     */
    private fun handlePhotoDeleted(photoId: String, path: String) {
        // 即使Fragment不可见时也能接收到这个事件
        LogUtils.d("PhotoLibrary", "处理照片删除: ID=$photoId")
        // 如果ViewModelScope不在运行中，重新加载照片列表
        val file = File(path)
        val photo = PhotoItem(
            id = "ID${file.name}",
            uri = Uri.fromFile(file),
            name = file.name,
            date = Date(file.lastModified())
        )
        viewModel.deletePhotos(photo)
    }
}

