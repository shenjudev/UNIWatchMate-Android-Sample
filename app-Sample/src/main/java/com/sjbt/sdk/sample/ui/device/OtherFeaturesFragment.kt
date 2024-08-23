package com.sjbt.sdk.sample.ui.device

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.base.api.UNIWatchMate
import com.base.sdk.entity.apps.WmConnectState
import com.base.sdk.entity.apps.WmFind
import com.base.sdk.exception.WmTransferException
import com.base.sdk.port.FileType
import com.base.sdk.port.State
import com.base.sdk.port.WmTransferState
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.NumberUtils
import com.github.kilnn.tool.widget.ktx.clickTrigger
import com.obsez.android.lib.filechooser.ChooserDialog
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BTConfig
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.data.device.flowStateConnected
import com.sjbt.sdk.sample.databinding.FragmentOtherFeaturesBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.utils.CacheDataHelper.setTransferring
import com.sjbt.sdk.sample.utils.PermissionHelper
import com.sjbt.sdk.sample.utils.ToastUtil
import com.sjbt.sdk.sample.utils.ToastUtil.showToast
import com.sjbt.sdk.sample.utils.launchRepeatOnStarted
import com.sjbt.sdk.sample.utils.launchWithLog
import com.sjbt.sdk.sample.utils.runCatchingWithLog
import com.sjbt.sdk.sample.utils.setAllChildEnabled
import com.sjbt.sdk.sample.utils.viewLifecycle
import com.sjbt.sdk.sample.utils.viewLifecycleScope
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import com.sjbt.sdk.utils.UriUtil
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await
import timber.log.Timber
import java.io.File

class OtherFeaturesFragment : BaseFragment(R.layout.fragment_other_features) {
    private val viewBind: FragmentOtherFeaturesBinding by viewBinding()
    private val deviceManager = Injector.getDeviceManager()
    private var isLocalUpdate = false
    private var otaFile: File? = null
    private val applicationScope = Injector.getApplicationScope()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycle.launchRepeatOnStarted {
            kotlin.run {
                deviceManager.flowStateConnected().collect {
                    viewBind.layoutContent.setAllChildEnabled(it)
                    if (!it) {
                        promptProgress.dismiss()
                    }
                }
            }
        }

        viewBind.itemFindDevice.clickTrigger {
            viewLifecycleScope.launchWhenStarted {
                val appFind = UNIWatchMate.wmApps.appFind.findWatch(WmFind(5, 5)).await()
                showToast("appFind $appFind")
            }
        }

        viewBind.itemStopFindDevice.clickTrigger {
            viewLifecycleScope.launchWhenStarted {
                val stopFind = UNIWatchMate.wmApps.appFind.stopFindWatch().await()
                showToast("stopFind $stopFind")
            }
        }

        viewBind.itemDeviceReset.clickTrigger {
            viewLifecycleScope.launchWhenStarted {
                deviceManager.reset()
            }
        }

        viewBind.itemDisconnect.clickTrigger {
            viewLifecycleScope.launchWhenStarted {
                deviceManager.disconnect()

//                UNIWatchMate.getBatteryInfo().subscribe({
//                    UNIWatchMate.wmLog.logE("battery", "电量信息:$it")
//                }, {
//                    UNIWatchMate.wmLog.logE("battery", "异常信息:" + it)
//                })
            }
        }

        viewBind.itemLocalOta.clickTrigger {
            viewLifecycleScope.launchWhenStarted {

                PermissionHelper.requestAppStorage(this@OtherFeaturesFragment) {
                    if (it) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager())) {
                            // Access to all files
//                            val uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
//                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
//                            startActivity(intent)
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                            intent.type = "application/octet-stream" // 设置文件类型为二进制文件
                            startForResult.launch(intent)
                        } else {
                            isLocalUpdate = true
//                            checkReadFilePermission()
                            showFileChooserDialog()
                        }
                    } else {
                        showToast(getString(R.string.permission_fail));
                    }
                }
            }
        }

        viewBind.reboot.clickTrigger {
            viewLifecycleScope.launchWhenStarted {
                deviceManager.reboot()
            }
        }
    }

    private val startForResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                result.data?.data?.let {

                    if (it.path!!.endsWith(".up") || it.path!!.endsWith(".upex")) {
                        val filePath: String =
                                UriUtil.getFileAbsolutePath(requireContext(), it)
                        val dialFile = File(filePath)
                        startLocalUpdate(dialFile)
                    } else {
                        ToastUtil.showToast(getString(R.string.error_selecting_file))
                    }


                }
            }
        }
    }

    private fun checkReadFilePermission() {
        PermissionHelper.requestAppStorage(this@OtherFeaturesFragment) {
            if (it) {
                showFileChooserDialog();
            } else {
                showToast(getString(R.string.permission_fail));
            }
        }
    }

    private fun showFileChooserDialog() {
        //choose a file
        activity?.let { ctx ->
            val chooserDialog = ChooserDialog(ctx, R.style.FileChooserStyle)
            chooserDialog
                    .withResources(
                            R.string.choose_file,
                            R.string.title_choose, R.string.dialog_cancel
                    )
                    .disableTitle(false)
                    .withFilter(false, false, BTConfig.UP, BTConfig.UP_EX)
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
            showToast(getString(R.string.error_up_file))
            return
        }
        if (file.length() < 100) { //长度小于100
            showToast(getString(R.string.error_up_file))
            return
        }
        val extension = FileUtils.getFileExtension(file)
        try {
            if (!TextUtils.isEmpty(extension)) {
                if (extension != BTConfig.UP && extension != BTConfig.UP_EX) {
                    showToast(getString(R.string.error_up_file))
                    return
                }
            }
            otaFile = file
            setTransferring(true)
            startOta()
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(getString(R.string.error_up_file))
            setTransferring(false)
            return
        }
    }

    private fun startOta() {
        applicationScope.launchWithLog {
            runCatchingWithLog {
                if (deviceManager.flowConnectorState.value != WmConnectState.BIND_SUCCESS) {
                    showToast(getString(R.string.device_state_disconnected))
                    return@launchWithLog
                }
                val fileList = mutableListOf<File>()
                otaFile?.let { otaFile ->
                    showInfoDialog(getString(R.string.action_updating))
                    fileList.add(otaFile)
                    val extension: String = FileUtils.getFileExtension(otaFile)
                    if (extension == BTConfig.UP) {
                        UNIWatchMate.wmTransferFile.startTransfer(FileType.OTA, fileList).asFlow()
                                .catch {
                                    hideInfoDialog()
                                    dealThrowable(it)
                                }
                                .collect {
                                    otaFileResult(it)
                                }
                    } else if (extension == BTConfig.UP_EX) {
                        UNIWatchMate.wmTransferFile.startTransfer(FileType.OTA_UPEX, fileList)
                                .asFlow()
                                .catch {
                                    hideInfoDialog()
                                    dealThrowable(it)
                                }
                                .collect {
                                    otaFileResult(it)
                                }
                    }

                }
            }
        }
    }

    private fun dealThrowable(it: Throwable) {
        if (it is WmTransferException) {
            showToast(it.error.name, true)
        } else {
            showToast(it.message, true)
        }

    }

    private fun otaFileResult(it: WmTransferState) {
        Timber.i("WmTransferState = ${it}")
        when (it.state) {
            State.PRE_TRANSFER -> {
            }

            State.TRANSFERRING -> {
                UNIWatchMate.wmLog.logI(
                        "OtherFeaturesFragment",
                        getString(R.string.action_updating_progress) + NumberUtils.format(
                                it.progress.toDouble(),
                                2
                        ) + "%"
                )
                if (isLocalUpdate) {
                    showInfoDialog(
                            getString(R.string.action_updating_progress) + NumberUtils.format(
                                    it.progress.toDouble(),
                                    2
                            ) + "%"
                    )
                    //                   promptProgress.showProgress()
                }
            }

            State.SUCCESS -> {
                hideInfoDialog()
                showToast(getString(R.string.tip_success))
                //                applicationScope.launchWithLog {
                //                    runCatchingWithLog {
                //                        deviceManager.delDevice()
                //                    }
                //                }
            }

            State.FAIL -> {
                hideInfoDialog()
                showToast(getString(R.string.tip_failed))
                //                applicationScope.launchWithLog {
                //                    runCatchingWithLog {
                //                        deviceManager.delDevice()
                //                    }
                //                }
            }
        }
    }

}