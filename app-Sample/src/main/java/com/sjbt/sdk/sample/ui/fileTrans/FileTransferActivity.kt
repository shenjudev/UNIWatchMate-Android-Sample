package com.sjbt.sdk.sample.ui.fileTrans

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.base.api.UNIWatchMate
import com.base.api.UNIWatchMate.wmTransferFile
import com.base.sdk.port.FileType
import com.base.sdk.port.State
import com.base.sdk.port.WmTransferState
import com.blankj.utilcode.util.FileUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.sjbt.sdk.entity.MsgBean
import com.sjbt.sdk.sample.MyApplication
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.Config
import com.sjbt.sdk.sample.data.device.flowStateConnected
import com.sjbt.sdk.sample.databinding.ActivityFileTransferBinding
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.base.BaseActivity
import com.sjbt.sdk.sample.dialog.CallBack
import com.sjbt.sdk.sample.ui.dialog.SendMusicDialog
import com.sjbt.sdk.sample.utils.*
import com.sjbt.sdk.sample.utils.CacheDataHelper.setTransferring
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import timber.log.Timber
import java.io.File
import java.util.*

val TAG = "FileTransferActivity"

class FileTransferActivity : BaseActivity(), View.OnClickListener,
    CancelTransferFileListener {
    private var mMusicAdapter: MusicAdapter? = null
    private val mLocalFileBeanList: MutableList<LocalFileBean> = ArrayList()
    private val mLocalMusicFileBeans: MutableList<LocalFileBean> = ArrayList()
    private val mLocalTxtFileBeans: MutableList<LocalFileBean> = ArrayList()
    private var mLocalVideoBeans: List<LocalFileBean>? = ArrayList()
    private var mSendMusicDialog: SendMusicDialog? = null
    private val mMediaAlbumContentObserver = MediaAlbumContentObserver(mHandler)
    private val mAudioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    private val mFileMusicSelected: MutableList<File> = LinkedList()
    private val mFileTxtSelected: MutableList<File> = LinkedList()
    private val mFileVideoSelected: MutableList<File> = LinkedList()
    private var mSendMusicCount = 0
    private var mSendTxtCount = 0
    private var mSendVideCount = 0
    private var mSelectMusicCount = 0
    private var mSelectTxtCount = 0
    private var mSelectVideoCount = 0
    private var cancelSend = false
    private var transferType = FileType.MUSIC
    private var footerView: View? = null
    private var tvCount: TextView? = null
    private var tvMemoryLimit: TextView? = null
    private val deviceManager = Injector.getDeviceManager()
    private var binding: ActivityFileTransferBinding? = null

    /**
     * 监听音乐列表变动
     */
    private fun initMediaContentObserver() {
//        getContentResolver().registerContentObserver(mAudioUri, true, mMediaAlbumContentObserver);
//        mMediaAlbumContentObserver.setOnChangeListener(new MediaAlbumContentObserver.OnChangeListener() {
//            @Override
//            public void onChange(boolean isAdd, Uri uri) {
////                binding.tvStatus.setText("本地音乐变化了：" + isAdd + "  " + new Random().nextInt());
//
//                if (isAdd) {
//                    return;
//                }
//
//                sendEndTransferFile();
//                hideDialog(mSendMusicDialog);
//
//                ThreadUtil.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        mLocalFileBeanList.clear();
//                        mFileMusicSelected.clear();
//                        mFileTxtSelected.clear();
//                        mLocalMusicFileBeans = AudioUtils.initLocalSongs(APP.getInstance());
//                        mLocalTxtFileBeans = AudioUtils.getLocalTxtFiles();
//
//                        if (transferType == FileType.MUSIC) {
//                            mLocalFileBeanList.addAll(mLocalMusicFileBeans);
//                        } else {
//                            mLocalFileBeanList.addAll(mLocalTxtFileBeans);
//                        }
//
//                        hideLoadingDlg();
//
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                mMusicAdapter.notifyDataSetChanged();
//                            }
//                        });
//                    }
//                });
//            }
//        });
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileTransferBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        init()

    }

    fun init() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //        initMediaContentObserver();
        binding!!.ivBack.setOnClickListener(this)
        binding!!.tvSend.setOnClickListener(this)
        binding!!.tvTitle.setText(R.string.file_transfer)

        AudioUtils.scanMusicFiles(MyApplication.instance)
        val linearLayoutManager = LinearLayoutManager(this)
        binding!!.appList.layoutManager = linearLayoutManager
        mMusicAdapter = MusicAdapter(R.layout.item_music, mLocalFileBeanList)
        binding!!.appList.adapter = mMusicAdapter
        footerView = LayoutInflater.from(this).inflate(R.layout.footer_file_count, null)
        tvCount = footerView!!.findViewById(R.id.tv_file_count)
        tvMemoryLimit = footerView!!.findViewById(R.id.tv_memory_limit)
        mMusicAdapter!!.addFooterView(footerView)
        val viewEmpty = LayoutInflater.from(this).inflate(R.layout.empty_view, null)
        val tvEmptyMusic = viewEmpty.findViewById<TextView>(R.id.tvEmptyMusic)
        val ivEmpty = viewEmpty.findViewById<ImageView>(R.id.ivEmpty)
        ivEmpty.setImageResource(R.mipmap.biu_icon_empty_music)
        tvEmptyMusic.setText(R.string.no_music_found)
        mMusicAdapter!!.emptyView = viewEmpty
        mMusicAdapter!!.setOnItemClickListener(BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val file = File(mLocalFileBeanList[position].fileUrl)
            if (file.length() > MAX_TRANSFER_LEN && transferType !== FileType.AVI) { //超过20M
                ToastUtil.showToast(getString(R.string.too_big_file))
                return@OnItemClickListener
            }
            mLocalFileBeanList[position].setSelected(!mLocalFileBeanList[position].isSelected())
            mMusicAdapter!!.notifyDataSetChanged()
            setTransferFileState()
        })
        if (binding!!.rbMusic.visibility == View.VISIBLE) {
            binding!!.rgFile.check(binding!!.rbMusic.id)
            defultChceked(1)
        } else if (binding!!.rbBook.visibility == View.VISIBLE) {
            binding!!.rgFile.check(binding!!.rbBook.id)
            defultChceked(2)
        } else if (binding!!.rbVideo.visibility == View.VISIBLE) {
            binding!!.rgFile.check(binding!!.rbVideo.id)
            defultChceked(3)
        }
        lifecycle.launchRepeatOnStarted {
            launch {
                deviceManager.flowStateConnected().collect {
                    if (it) {
                        hideConfirmDialog()
                    } else {
                        mHandler.postDelayed({ hideDialog(mSendMusicDialog) }, 1500)
                        showDisableView()
                    }
                }
            }
        }
//        showLoadingDlg();
//        ThreadUtil.execute(new Runnable() {
//            @Override
//            public void run() {
//
//                mLocalMusicFileBeans.addAll(AudioUtils.initLocalSongs(APP.getInstance()));
//                if (CacheDataHelper.INSTANCE.getActionSupportBean().ebookSupportState == 1) {
//                    mLocalTxtFileBeans.addAll(AudioUtils.getLocalTxtFiles());
//                }
//
//                if (CacheDataHelper.INSTANCE.getActionSupportBean().videoSupportState == 1){
//                    mLocalVideoBeans = GetVideoListUtils.INSTANCE.getInstance().loadPageMediaData(MusicManageActivity.this);
//                }
//
//                mLocalFileBeanList.clear();
//                if (CacheDataHelper.INSTANCE.getActionSupportBean().musicSupportState == 1) {
//                    mLocalFileBeanList.addAll(mLocalMusicFileBeans);
//                } else if (CacheDataHelper.INSTANCE.getActionSupportBean().ebookSupportState == 1) {
//                    mLocalFileBeanList.addAll(mLocalTxtFileBeans);
//                } else if (CacheDataHelper.INSTANCE.getActionSupportBean().videoSupportState == 1){
//                    mLocalFileBeanList.addAll(mLocalVideoBeans);
//                }
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (binding.rgFile.getCheckedRadioButtonId() == R.id.rb_music){
//                            updateFileCount(mLocalFileBeanList.size());
//                            mMusicAdapter.notifyDataSetChanged();
//                        }
//                        hideLoadingDlg();
//                    }
//                });
//            }
//        });
        binding!!.rgFile.setOnCheckedChangeListener { radioGroup, checkId ->
            Log.e("Send", "checkId：$checkId")
            if (checkId == R.id.rb_music) {
                tvMemoryLimit?.setText(getString(R.string.file_memory_size_tips))
                transferType = FileType.MUSIC
                if (mLocalMusicFileBeans.isEmpty()) {
                    showLoadingDlg()
                    Observable.create<Boolean> { emitter ->
                        mLocalMusicFileBeans.addAll(AudioUtils.initLocalSongs(MyApplication.instance))
                        emitter.onNext(true)
                        emitter.onComplete()
                    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : Observer<Boolean> {
                            override fun onSubscribe(d: Disposable) {}
                            override fun onNext(aBoolean: Boolean) {
                                mLocalFileBeanList.clear()
                                mLocalFileBeanList.addAll(mLocalMusicFileBeans)
                                mMusicAdapter!!.notifyDataSetChanged()
                                hideLoadingDlg()
                            }

                            override fun onError(e: Throwable) {}
                            override fun onComplete() {
                                updateFileCount(mLocalFileBeanList.size)
                                setTransferFileState()
                            }
                        })
                } else {
                    mLocalFileBeanList.clear()
                    mLocalFileBeanList.addAll(mLocalMusicFileBeans)
                    mMusicAdapter!!.notifyDataSetChanged()
                }
            } else if (checkId == R.id.rb_book) {
                tvMemoryLimit?.setText(getString(R.string.file_memory_size_tips))
                transferType = FileType.TXT
                if (mLocalTxtFileBeans.isEmpty()) {
                    showLoadingDlg()
                    mMusicAdapter!!.data.clear()
                    Observable.create<Boolean> { emitter ->
                        Log.e("Send-电子书", System.currentTimeMillis().toString() + "==========1")
                        mLocalTxtFileBeans.addAll(AudioUtils.getLocalDwonloadTxtFiles())
                        Log.e("Send-电子书", System.currentTimeMillis().toString() + "==========2")
                        emitter.onNext(true)
                        emitter.onComplete()
                    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : Observer<Boolean> {
                            override fun onSubscribe(d: Disposable) {}
                            override fun onNext(aBoolean: Boolean) {
                                mLocalFileBeanList.clear()
                                mLocalFileBeanList.addAll(mLocalTxtFileBeans)
                                mMusicAdapter!!.notifyDataSetChanged()
                                Log.e(
                                    "Send-电子书",
                                    System.currentTimeMillis().toString() + "==========3"
                                )
                                hideLoadingDlg()
                            }

                            override fun onError(e: Throwable) {}
                            override fun onComplete() {
                                updateFileCount(mLocalFileBeanList.size)
                                setTransferFileState()
                            }
                        })
                } else {
                    mLocalFileBeanList.clear()
                    mLocalFileBeanList.addAll(mLocalTxtFileBeans)
                    mMusicAdapter!!.notifyDataSetChanged()
                }
            } else if (checkId == R.id.rb_video) {
                transferType = FileType.AVI
                tvMemoryLimit?.setText(getString(R.string.assigned_video))
                if (mLocalVideoBeans!!.isEmpty()) {
                    showLoadingDlg()
                    Observable.create<List<LocalFileBean>> { emitter ->
                        mLocalVideoBeans =
                            GetVideoListUtils.instance.loadPageMediaData(this@FileTransferActivity)
                        emitter.onNext(mLocalVideoBeans)
                        emitter.onComplete()
                    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : Observer<List<LocalFileBean>> {
                            override fun onSubscribe(d: Disposable) {}
                            override fun onNext(o: List<LocalFileBean>) {
                                hideLoadingDlg()
                                mLocalFileBeanList.clear()
                                mLocalFileBeanList.addAll(o)
                                mMusicAdapter!!.notifyDataSetChanged()
                            }

                            override fun onError(e: Throwable) {}
                            override fun onComplete() {
                                updateFileCount(mLocalFileBeanList.size)
                                setTransferFileState()
                            }
                        })
                } else {
                    mLocalFileBeanList.clear()
                    mMusicAdapter!!.data.clear()
                    mLocalFileBeanList.addAll(mLocalVideoBeans!!)
                    mMusicAdapter!!.notifyDataSetChanged()
                }
            }
            updateFileCount(mLocalFileBeanList.size)
            setTransferFileState()
        }
    }

    private fun updateFileCount(count: Int) {
        tvCount!!.text = String.format(getString(R.string.file_count), count)
    }

    private fun setTransferFileState() {
        if (transferType === FileType.MUSIC) {
            mSelectMusicCount = 0
            for (localFileBean in mLocalFileBeanList) {
                if (localFileBean.isSelected()) {
                    mSelectMusicCount++
                }
            }
            if (mSelectMusicCount == 0) {
                binding!!.tvSend.text = getString(R.string.send_no_count)
            } else {
                binding!!.tvSend.text =
                    String.format(getString(R.string.send_with_count), mSelectMusicCount)
            }
        } else if (transferType === FileType.TXT) {
            mSelectTxtCount = 0
            for (localFileBean in mLocalFileBeanList) {
                if (localFileBean.isSelected()) {
                    mSelectTxtCount++
                }
            }
            if (mSelectTxtCount == 0) {
                binding!!.tvSend.text = getString(R.string.send_no_count)
            } else {
                binding!!.tvSend.text =
                    String.format(getString(R.string.send_with_count), mSelectTxtCount)
            }
        } else if (transferType === FileType.AVI) {
            mSelectVideoCount = 0
            for (localFileBean in mLocalFileBeanList) {
                if (localFileBean.isSelected()) {
                    mSelectVideoCount++
                }
            }
            if (mSelectVideoCount == 0) {
                binding!!.tvSend.text = getString(R.string.send_no_count)
            } else {
                binding!!.tvSend.text =
                    String.format(getString(R.string.send_with_count), mSelectVideoCount)
            }
        }
    }

    override fun onCancelResult(result: Boolean) {}
    override fun onTimeOut(msgBean: MsgBean) {}
    private fun transferEnd() {
        try {
            hideLoadingDlg()
            setTransferring(false)
            if (mSendMusicDialog!!.isShowing) {
                if (transferType === FileType.MUSIC) {
                    mSendMusicDialog!!.showFinish(mSendMusicCount, mSelectMusicCount)
                } else if (transferType === FileType.TXT) {
                    mSendMusicDialog!!.showFinish(mSendTxtCount, mSelectTxtCount)
                } else if (transferType === FileType.AVI) {
                    mSendMusicDialog!!.showFinish(mSendVideCount, mSelectVideoCount)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(v: View) {
        if (v == binding!!.ivBack) {

//            if (mSendMusicDialog.isSending()) {
//                showConfirmDialogWithCallback(getString(R.string.cancel_sending), getString(R.string.submit), new CallBack() {
//                    @Override
//                    public void callBack(Object o) {
//                        sendEndTransferFile();
//                        CacheDataHelper.INSTANCE.setTransferring(false);
//                        finish();
//                    }
//                });
//            } else {
            wmTransferFile.cancelTransfer().subscribe()
            setTransferring(false)
            finish()
            //            }
        } else if (v == binding!!.tvSend) {
            if (SystemUtil.isFastClick()) {
                return
            }
            if (mSendMusicDialog != null && mSendMusicDialog!!.isShowing) {
                mSendMusicDialog!!.dismiss()
                mSendMusicDialog = null
            }
            mSendMusicDialog = SendMusicDialog(this
            ) { o ->
                when (o) {
                    Config.CLICK_CANCEL -> {
                        sendEndTransferFile()
                        wmTransferFile.cancelTransfer().subscribe()
                    }
                    Config.CLICK_RETRY -> prepareSendFiles()
                }
            }

            prepareSendFiles()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun prepareSendFiles() {
        if (transferType === FileType.MUSIC) {
            mFileMusicSelected.clear()
            for (localFileBean in mLocalFileBeanList) {
                if (localFileBean.isSelected()) {
                    val file = File(localFileBean.fileUrl)
                    mFileMusicSelected.add(file)
                }
            }
            if (mFileMusicSelected.size > 0) {
                var totalLen = 0L
                for (file in mFileMusicSelected) {
                    totalLen = totalLen + file.length()
                }
                if (totalLen > MAX_TRANSFER_LEN) {
                    ToastUtil.showToast(getString(R.string.max_transfer_tips))
                    return
                }
                setTransferring(true)
                sendStartTransferFile(mFileMusicSelected)
                if (mSendMusicDialog != null) {
                    mSendMusicDialog!!.show()
                }
            } else {
                setTransferring(false)
                ToastUtil.showToast(getString(R.string.choose_one_file))
            }
        } else if (transferType === FileType.TXT) {
            mFileTxtSelected.clear()
            for (localFileBean in mLocalFileBeanList) {
                if (localFileBean.isSelected()) {
                    val file = File(localFileBean.fileUrl)
                    mFileTxtSelected.add(file)
                }
            }
            if (mFileTxtSelected.size > 0) {
                var totalLen = 0L
                for (file in mFileTxtSelected) {
                    totalLen += file.length()
                }
                if (totalLen > MAX_TRANSFER_LEN) {
                    ToastUtil.showToast(getString(R.string.max_transfer_tips))
                    return
                }
                setTransferring(true)
                sendStartTransferFile(mFileTxtSelected)
            } else {
                setTransferring(false)
                ToastUtil.showToast(getString(R.string.choose_one_file))
            }
        } else if (transferType === FileType.AVI) {
            mFileVideoSelected.clear()
            for (localFileBean in mLocalFileBeanList) {
                if (localFileBean.isSelected()) {
                    val file = File(localFileBean.fileUrl)
                    mFileVideoSelected.add(file)
                }
            }
            if (mFileVideoSelected.size > 0) {
                var totalLen = 0L
                for (file in mFileVideoSelected) {
                    totalLen = totalLen + file.length()
                }

//                if (totalLen > MAX_TRANSFER_LEN && transferType != FileType.AVI) {
//                    ToastUtil.showToast(getString(R.string.max_transfer_tips));
//                    return;
//                }
                setTransferring(true)
                sendStartTransferFile(mFileVideoSelected)
            } else {
                setTransferring(false)
                ToastUtil.showToast(getString(R.string.choose_one_file))
            }
        }
    }

    private fun sendStartTransferFile(fileSelected: MutableList<File>) {
        cancelSend = false
        lifecycle.launchRepeatOnStarted {
            try {
                wmTransferFile.startTransfer(transferType, fileSelected).asFlow()
                    .onCompletion {

                    }.catch {
                        it.printStackTrace()
                        ToastUtil.showToast(it.message)
                    }.flowOn(Dispatchers.Main).collect { wmTransferState: WmTransferState ->
                        if (wmTransferState.state === State.PRE_TRANSFER) {
                        } else if (wmTransferState.state === State.TRANSFERRING) {
                            if (wmTransferState.progress == 100) { //step finish
                                cancelSend = false
                                if (transferType === FileType.MUSIC) {
                                    mSendMusicCount = wmTransferState.index
                                } else if (transferType === FileType.TXT) {
                                    mSendTxtCount = wmTransferState.index
                                } else if (transferType === FileType.AVI) {
                                    mSendVideCount = wmTransferState.index
                                }
                                mMusicAdapter!!.notifyDataSetChanged()
                            } else {
                                if (transferType === FileType.MUSIC) {
                                    mSendMusicCount = wmTransferState.index
                                } else if (transferType === FileType.MUSIC) {
                                    mSendTxtCount = wmTransferState.index
                                } else if (transferType === FileType.MUSIC) {
                                    mSendVideCount = wmTransferState.index
                                }
                                var name: String? = ""
                                if (wmTransferState.sendingFile != null) {
                                    name = FileUtils.getFileName(wmTransferState.sendingFile)
                                }
                                mSendMusicDialog!!.updateProgress(
                                    name,
                                    wmTransferState.index,
                                    wmTransferState.total,
                                    wmTransferState.progress.toDouble(),
                                    cancelSend
                                )
                            }
                        } else if (wmTransferState.state === State.SUCCESS) {
                            if (transferType === FileType.MUSIC) {
                                mSendMusicCount = wmTransferState.index
                            } else if (transferType === FileType.TXT) {
                                mSendTxtCount = wmTransferState.index
                            } else if (transferType === FileType.AVI) {
                                mSendVideCount = wmTransferState.index
                            }
                            Timber.d( "State.FINISH)"
                            )

                            if(wmTransferState.index == fileSelected.size){
                                transferEnd()
                                fileSelected.clear()
                            }

                            binding!!.tvSend.text = getString(R.string.send_no_count)
                            for (localFileBean in mLocalFileBeanList) {
                                localFileBean.setSelected(false)
                            }
                            mMusicAdapter!!.notifyDataSetChanged()
                            if (transferType === FileType.MUSIC) {
                                mSendMusicCount = 0
                                for (localFileBean in mLocalMusicFileBeans) {
                                    localFileBean.setSelected(false)
                                }
                            } else if (transferType === FileType.TXT) {
                                mSendTxtCount = 0
                                for (localFileBean in mLocalTxtFileBeans) {
                                    localFileBean.setSelected(false)
                                }
                            } else if (transferType === FileType.AVI) {
                                mSendVideCount = 0
                                for (localFileBean in mLocalVideoBeans!!) {
                                    localFileBean.setSelected(false)
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.showToast(e.message)
            }
        }
    }


    private fun sendEndTransferFile() {
        Timber.d(  "发送结束命令"
        )

        hideLoadingDlg()
        setTransferring(false)
    }


    private fun showDisableView() {
        hideLoadingDlg()
        setTransferring(false)
        showConfirmDialogWithCallback(getString(R.string.disconnect_tips),
            getString(R.string.submit), object : CallBack<String> {
                override fun callBack(o: String) {
                    setTransferring(false)
                    finish()
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Timber.d(  "$localClassName onDestroy")
        sendEndTransferFile()
        hideDialog(mSendMusicDialog)
    }

    private fun defultChceked(status: Int) {
        when (status) {
            1 -> {
                mLocalMusicFileBeans.addAll(AudioUtils.initLocalSongs(MyApplication.instance))
                transferType = FileType.MUSIC
                tvMemoryLimit!!.text = getString(R.string.file_memory_size_tips)
            }

            2 -> {
                mLocalTxtFileBeans.addAll(AudioUtils.getLocalDwonloadTxtFiles())
                transferType = FileType.TXT
                tvMemoryLimit!!.text = getString(R.string.file_memory_size_tips)
            }

            3 -> {
                mLocalVideoBeans =
                    GetVideoListUtils.instance.loadPageMediaData(this@FileTransferActivity)
                transferType = FileType.AVI
                tvMemoryLimit!!.text = getString(R.string.assigned_video)
            }
        }
        showLoadingDlg()
        Observable.create<Boolean> { emitter ->
            when (status) {
                1 -> mLocalMusicFileBeans.addAll(AudioUtils.initLocalSongs(MyApplication.instance))
                2 -> mLocalTxtFileBeans.addAll(AudioUtils.getLocalDwonloadTxtFiles())
                3 -> mLocalVideoBeans =
                    GetVideoListUtils.instance.loadPageMediaData(this@FileTransferActivity)
            }
            emitter.onNext(true)
            emitter.onComplete()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Boolean> {
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(aBoolean: Boolean) {
                    mLocalFileBeanList.clear()
                    mLocalFileBeanList.addAll(mLocalMusicFileBeans)
                    mMusicAdapter!!.notifyDataSetChanged()
                    hideLoadingDlg()
                }

                override fun onError(e: Throwable) {}
                override fun onComplete() {
                    updateFileCount(mLocalFileBeanList.size)
                    setTransferFileState()
                }
            })
    }

    companion object {
        private const val MAX_TRANSFER_LEN = (20 * 1024 * 1024).toLong()
        fun launchActivity(context: Activity) {
            val intent = Intent(context, FileTransferActivity::class.java)
            context.startActivity(intent)
            context.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
        }
    }
}