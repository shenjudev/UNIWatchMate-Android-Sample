package com.sjbt.sdk.sample.ui.device.dial.library

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.apps.WmDial
import com.blankj.utilcode.util.FileUtils
import com.github.kilnn.tool.ui.DisplayUtil
import com.obsez.android.lib.filechooser.ChooserDialog
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.AsyncViewModel
import com.sjbt.sdk.sample.base.BTConfig
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.base.Fail
import com.sjbt.sdk.sample.base.Loading
import com.sjbt.sdk.sample.base.SingleAsyncState
import com.sjbt.sdk.sample.base.Success
import com.sjbt.sdk.sample.data.device.isConnected
import com.sjbt.sdk.sample.databinding.FragmentDialLibraryBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.di.internal.SingleInstance.deviceManager
import com.sjbt.sdk.sample.model.user.DialMock
import com.sjbt.sdk.sample.ui.device.dial.DialEvent
import com.sjbt.sdk.sample.ui.device.dial.DialInstalledViewModel
import com.sjbt.sdk.sample.utils.CacheDataHelper
import com.sjbt.sdk.sample.utils.PermissionHelper
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.launchWithLog
import com.sjbt.sdk.sample.utils.runCatchingWithLog
import com.sjbt.sdk.sample.utils.showFailed
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.sample.widget.GridSpacingItemDecoration
import com.sjbt.sdk.sample.widget.LoadingView
import com.sjbt.sdk.utils.UriUtil
import kotlinx.coroutines.launch
import java.io.File

class DialLibraryFragment : BaseFragment(R.layout.fragment_dial_library) {

    private val viewBind: FragmentDialLibraryBinding by viewBinding()
    private val dfuViewModel: DfuViewModel by viewModels()
    private val dialInstalledViewModel: DialInstalledViewModel by viewModels()
    private val dialLibraryViewModel: DialLibraryViewModel by viewModels()
    private var wmDials: MutableList<WmDial>? = mutableListOf()
    private lateinit var adapter: DialLibraryAdapter
    private var dialFile: File? = null
    private val applicationScope = Injector.getApplicationScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = DialLibraryAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBind.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        viewBind.recyclerView.addItemDecoration(
            GridSpacingItemDecoration(
                3,
                DisplayUtil.dip2px(requireContext(), 15F),
                true
            )
        )
        adapter.listener = object : DialLibraryAdapter.Listener {

            override fun onItemClick(packet: DialMock) {
                if (Injector.getDeviceManager().isConnected()) {
                    if (packet.installed == 1) {
                        promptToast.showInfo(R.string.ds_dial_installed)
                    } else {
                        DialLibraryDfuDialogFragment.newInstance(packet)
                            .show(childFragmentManager, null)
                    }
                } else {
                    promptToast.showInfo(R.string.device_state_disconnected)
                }
            }
        }
        viewBind.recyclerView.adapter = adapter
        viewBind.loadingView.listener = LoadingView.Listener {
            dialInstalledViewModel.requestInstallDials()
        }
        viewBind.btnLocalDial.setOnClickListener {

            PermissionHelper.requestAppStorage(this@DialLibraryFragment) {
                if (it) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // Access to all files
//                        val uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
//                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
//                        startActivity(intent)
                        // 打开文件选择器

                        // 打开文件选择器
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                        intent.type = "application/octet-stream" // 设置文件类型为二进制文件
                        startForResult.launch(intent)
                    } else {
                        localDialSelect()
                    }
                } else {
                    ToastUtil.showToast(getString(R.string.permission_fail));
                }
            }
        }
        viewLifecycle.launchRepeatOnStarted {
            launch {
                dialInstalledViewModel.flowState.collect { state ->
                    when (state.requestDials) {
                        is Loading -> {
                            viewBind.loadingView.showLoading()
                        }

                        is Fail -> {
                            viewBind.loadingView.showError(R.string.tip_load_error)
                        }

                        is Success -> {
                            wmDials = state.requestDials()
                            adapter.items = dialLibraryViewModel.refreshInternal(wmDials)
                            adapter.notifyDataSetChanged()
                            viewBind.loadingView.visibility = View.GONE
                        }

                        else -> {}
                    }
                }
            }
            launch {
                dialInstalledViewModel.flowEvent.collect { event ->
                    when (event) {
                        is DialEvent.RequestFail -> {
                            promptToast.showFailed(event.throwable)
                        }
                        else ->{}
                    }
                }
            }
            launch {
                dfuViewModel.flowDfuEvent.collect { event ->
                    when (event) {
                        is DfuViewModel.DfuEvent.OnSuccess -> {
                            wmDials?.let {
                                val intalledDialMock = event.installed
                                if (intalledDialMock.dialCoverRes >= 0) {
                                    it.add(WmDial(intalledDialMock.id, 0, true))
                                    adapter.items = dialLibraryViewModel.refreshInternal(it)
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        }

                        else ->{}

                    }
                }
            }
        }
    }

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        when (result.resultCode) {
            RESULT_OK -> {
                result.data?.data?.let {

                    if (it.path!!.endsWith(".dial")){
                        val filePath: String =
                            UriUtil.getFileAbsolutePath(requireContext(), it)
                        val dialFile = File(filePath)
                        startLocalUpdate(dialFile)
                    }else{
                        ToastUtil.showToast(getString(R.string.error_selecting_file))
                    }


                }
            }
        }
    }

    private fun localDialSelect() {
        //choose a file
        activity?.let { ctx ->
            val chooserDialog = ChooserDialog(ctx, R.style.FileChooserStyle)
            chooserDialog
                .withResources(
                    R.string.choose_file,
                    R.string.title_choose, R.string.dialog_cancel
                )
                .disableTitle(false)
                .withFilter(false, false, BTConfig.DIAL)
                .enableOptions(false)
                .titleFollowsDir(false)
                .cancelOnTouchOutside(false)
                .displayPath(true)
                .enableDpad(true)

            chooserDialog.withNegativeButtonListener { dialog, which ->
            }

            chooserDialog.withChosenListener { dir, dirFile ->
//            Toast.makeText(ctx, (dirFile.isDirectory() ? "FOLDER: " : "FILE: ") + dir,
//                    Toast.LENGTH_SHORT).show();
                if (!dirFile.isFile) {
                    return@withChosenListener
                }
                val file: File = dirFile
                startLocalUpdate(file)
            }

            chooserDialog.withOnBackPressedListener { dialog -> chooserDialog.goBack() }
            chooserDialog.show()
        }
    }

    private fun startLocalUpdate(file: File?) {
        if (file == null || !file.exists()) {
            ToastUtil.showToast(getString(R.string.error_up_file))
            return
        }
        if (file.length() < 100) { //长度小于100
            ToastUtil.showToast(getString(R.string.error_up_file))
            return
        }
        val extension = FileUtils.getFileExtension(file)
        try {
            if (!TextUtils.isEmpty(extension)) {
                if (extension != BTConfig.DIAL) {
                    ToastUtil.showToast(getString(R.string.error_up_file))
                    return
                }
            }
            dialFile = file
            CacheDataHelper.setTransferring(true)
            startOta()
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtil.showToast(getString(R.string.error_up_file))
            CacheDataHelper.setTransferring(false)
            return
        }
    }

    private fun startOta() {
        applicationScope.launchWithLog {
            runCatchingWithLog {
                if (deviceManager.flowConnectorState.value != WmConnectState.BIND_SUCCESS) {
                    ToastUtil.showToast(getString(R.string.device_state_disconnected))
                    return@launchWithLog
                }
                val fileList = mutableListOf<File>()
                dialFile?.let { file ->
                    fileList.add(file)
                    val extension: String = FileUtils.getFileExtension(file)
                    if (extension == BTConfig.DIAL) {
                        val dialMock = DialMock(-1, file.absolutePath, -1, "")
                        DialLibraryDfuDialogFragment.newInstance(dialMock)
                            .show(childFragmentManager, null)
                    }
                }
            }
        }
    }
}

data class PushParamsAndPackets(
    val packets: List<DialMock>,
) {
    override fun toString(): String {
        return ", packets size:${packets.size}"
    }
}

/**
 * Request and combine [DialPushParams] and [DialPacket] list
 */
class DialLibraryViewModel(
) : AsyncViewModel<SingleAsyncState<PushParamsAndPackets>>(SingleAsyncState()) {

    fun refreshInternal(wmDials: MutableList<WmDial>?): MutableList<DialMock> {
        val packets = mutableListOf<DialMock>()
        packets.add(
            DialMock(
                R.mipmap.a8c637a6c26d476db361051786e773df7,
                "8c637a6c26d476db361051786e773df7.dial",
                installDialsContain(wmDials, "8c637a6c26d476db361051786e773df7"),
                "8c637a6c26d476db361051786e773df7"
            )
        )
        packets.add(
            DialMock(
                R.mipmap.a59c4aad46ed434ca58786f3232aba673_california_simple,
                "59c4aad46ed434ca58786f3232aba673.dial",
                installDialsContain(wmDials, "59c4aad46ed434ca58786f3232aba673"),
                "59c4aad46ed434ca58786f3232aba673"
            )
        )
//        packets.add(
//            DialMock(
//                R.mipmap.a4974f889d52c4a519eac9ea409b3295c,
//                "4974f889d52c4a519eac9ea409b3295c.dial",installDialsContain(wmDials,"4974f889d52c4a519eac9ea409b3295c")
//            )
//        )
        packets.add(
            DialMock(
                R.mipmap.a1245156a62de4d6d8d60d8f8ff751302,
                "1245156a62de4d6d8d60d8f8ff751302.dial",
                installDialsContain(wmDials, "1245156a62de4d6d8d60d8f8ff751302"),
                "1245156a62de4d6d8d60d8f8ff751302"
            )
        )
        packets.add(
            DialMock(
                R.mipmap.aaab168c15c7b40eab361ca98fdd213ee,
                "aab168c15c7b40eab361ca98fdd213ee.dial",
                installDialsContain(wmDials, "aab168c15c7b40eab361ca98fdd213ee"),
                "aab168c15c7b40eab361ca98fdd213ee"
            )
        )
        return packets
    }

    private fun installDialsContain(wmDials: MutableList<WmDial>?, s: String): Int {
        wmDials?.let {
            for (bean in wmDials) {
                if (s.contains(bean.id)) {
                    return 1
                }
            }
        }

        return 0

    }


}