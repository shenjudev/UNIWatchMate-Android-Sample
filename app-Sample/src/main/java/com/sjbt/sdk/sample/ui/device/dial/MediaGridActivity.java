package com.sjbt.sdk.sample.ui.device.dial;


import static com.transsion.imagepicker.PickerConfig.EXTRAS_IMAGES;
import static com.transsion.imagepicker.PickerConfig.EXTRAS_TAKE_PICKERS;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.sjbt.sdk.sample.R;
import com.transsion.imagepicker.DataHolder;
import com.transsion.imagepicker.ImagePicker;
import com.transsion.imagepicker.MediaDataSource;
import com.transsion.imagepicker.adapter.ImageFolderAdapter;
import com.transsion.imagepicker.adapter.ImageRecyclerAdapter;
import com.transsion.imagepicker.bean.ImageFolder;
import com.transsion.imagepicker.bean.ImageItem;
import com.transsion.imagepicker.ui.ImageBaseActivity;
import com.transsion.imagepicker.ui.ImageCropActivity;
import com.transsion.imagepicker.ui.ImagePreviewActivity;
import com.transsion.imagepicker.util.Utils;
import com.transsion.imagepicker.view.FolderPopUpWindow;
import com.transsion.imagepicker.view.GridSpacingItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：
 * 修订历史：
 * 2017-03-17
 *
 * @author nanchen
 * 新增可直接传递是否裁剪参数，以及直接拍照
 * ================================================
 */
public class MediaGridActivity extends ImageBaseActivity implements MediaDataSource.OnImagesLoadedListener,
        ImageRecyclerAdapter.OnImageItemClickListener, ImagePicker.OnImageSelectedListener, View.OnClickListener {
    public static final int REQUEST_PERMISSION_STORAGE = 0x01;
    public static final int REQUEST_PERMISSION_CAMERA = 0x02;

    private ImagePicker imagePicker;
    private boolean isOrigin = false;  //是否选中原图
    private View mFooterBar;     //底部栏
    private TextView mBtnOk;       //确定按钮
    private View mllDir; //文件夹切换按钮
    private TextView mtvDir; //显示当前文件夹
    private TextView mBtnPre;      //预览按钮
    private ImageFolderAdapter mImageFolderAdapter;    //图片文件夹的适配器
    private FolderPopUpWindow mFolderPopupWindow;  //ImageSet的PopupWindow
    private List<ImageFolder> mImageFolders;   //所有的图片文件夹
    //    private ImageGridAdapter mImageGridAdapter;  //图片九宫格展示的适配器
    private boolean directPhoto = false; // 默认不是直接调取相机
    private RecyclerView mRecyclerView;
    private ImageRecyclerAdapter mRecyclerAdapter;
    private MediaDataSource mImageDataSource;
    private View mLayoutLoading;

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        directPhoto = savedInstanceState.getBoolean(EXTRAS_TAKE_PICKERS, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRAS_TAKE_PICKERS, directPhoto);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_grid);
        getSupportActionBar().hide();
        imagePicker = ImagePicker.getInstance();
        imagePicker.clear();
        imagePicker.addOnImageSelectedListener(this);
        Intent data = getIntent();
        // 新增可直接拍照
        if (data != null && data.getExtras() != null) {
            directPhoto = data.getBooleanExtra(EXTRAS_TAKE_PICKERS, false); // 默认不是直接打开相机
            if (directPhoto) {
                if (!(checkPermission(Manifest.permission.CAMERA))) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MediaGridActivity.REQUEST_PERMISSION_CAMERA);
                } else {
                    imagePicker.takePicture(this, ImagePicker.REQUEST_CODE_TAKE);
                }
            }
            ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(EXTRAS_IMAGES);
            imagePicker.setSelectedImages(images);
        }

        mRecyclerView = findViewById(R.id.recycler);
        mLayoutLoading = findViewById(R.id.layout_loading);

        findViewById(R.id.btn_back).setOnClickListener(this);
        mBtnOk = findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(this);
        mBtnPre = findViewById(R.id.btn_preview);
        mBtnPre.setOnClickListener(this);
        mFooterBar = findViewById(R.id.footer_bar);
        mllDir = findViewById(R.id.ll_dir);
        mllDir.setOnClickListener(this);
        mtvDir = findViewById(R.id.tv_dir);
        if (imagePicker.isMultiMode()) {
            mBtnOk.setVisibility(View.VISIBLE);
            mBtnPre.setVisibility(View.VISIBLE);
        } else {
            mBtnOk.setVisibility(View.GONE);
            mBtnPre.setVisibility(View.GONE);
        }

        mImageFolderAdapter = new ImageFolderAdapter(this, null);
        mRecyclerAdapter = new ImageRecyclerAdapter(this, null);
        mRecyclerAdapter.setOnImageItemClickListener(MediaGridActivity.this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(MediaGridActivity.this, 3));
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, Utils.dp2px(MediaGridActivity.this, 5), false));
        mRecyclerView.setAdapter(mRecyclerAdapter);

        onImageSelected(0, null, false);
        String[] permissions = getStoragePermission();
        if (checkPermission(permissions)) {
            mLayoutLoading.setVisibility(View.VISIBLE);
            mImageDataSource = new MediaDataSource(this, null, this);
        } else {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLayoutLoading.setVisibility(View.VISIBLE);
                mImageDataSource = new MediaDataSource(this, null, this);
            } else {
                Log.i("ImageGridActivity", "权限被禁止，无法选择本地图片");
            }
        } else if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                imagePicker.takePicture(this, ImagePicker.REQUEST_CODE_TAKE);
            } else {
                Log.i("ImageGridActivity", "权限被禁止，无法打开相机");
            }
        }
    }

    @Override
    protected void onDestroy() {
        imagePicker.removeOnImageSelectedListener(this);
        if (mImageDataSource != null) {
            mImageDataSource.setLoadedListener(null);
            mImageDataSource = null;
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_ok) {
            Intent intent = new Intent();
            intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
            setResult(ImagePicker.RESULT_CODE_ITEMS, intent);  //多选不允许裁剪裁剪，返回数据
            finish();
        } else if (id == R.id.ll_dir) {
            if (mImageFolders == null) {
                Log.i("ImageGridActivity", "您的手机没有图片");
                return;
            }
            //点击文件夹按钮
            createPopupFolderList();
            mImageFolderAdapter.refreshData(mImageFolders);  //刷新数据
            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            } else {
                mFolderPopupWindow.showAtLocation(mFooterBar, Gravity.NO_GRAVITY, 0, 0);
                //默认选择当前选择的上一个，当目录很多时，直接定位到已选中的条目
                int index = mImageFolderAdapter.getSelectIndex();
                index = index == 0 ? index : index - 1;
                mFolderPopupWindow.setSelection(index);
            }
        } else if (id == R.id.btn_preview) {
            Intent intent = new Intent(MediaGridActivity.this, ImagePreviewActivity.class);
            intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, 0);
            intent.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, imagePicker.getSelectedImages());
            intent.putExtra(ImagePreviewActivity.ISORIGIN, isOrigin);
            intent.putExtra(ImagePicker.EXTRA_FROM_ITEMS, true);
            startActivityForResult(intent, ImagePicker.REQUEST_CODE_PREVIEW);
        } else if (id == R.id.btn_back) {
            //点击返回按钮
            finish();
        }
    }

    /**
     * 创建弹出的ListView
     */
    private void createPopupFolderList() {
        mFolderPopupWindow = new FolderPopUpWindow(this, mImageFolderAdapter);
        mFolderPopupWindow.setOnItemClickListener(new FolderPopUpWindow.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mImageFolderAdapter.setSelectIndex(position);
                imagePicker.setCurrentImageFolderPosition(position);
                mFolderPopupWindow.dismiss();
                ImageFolder imageFolder = (ImageFolder) adapterView.getAdapter().getItem(position);
                if (null != imageFolder) {
//                    mImageGridAdapter.refreshData(imageFolder.images);
                    mRecyclerAdapter.refreshData(imageFolder.images);
                    mtvDir.setText(imageFolder.name);
                }
            }
        });
        mFolderPopupWindow.setMargin(0);
    }

    @Override
    public void onImagesLoaded(List<ImageFolder> imageFolders) {
        if (mRecyclerView == null) {
            return;
        }
        mRecyclerView.post(() -> {
            if (mRecyclerView == null) {
                return;
            }
            mLayoutLoading.setVisibility(View.GONE);
            mImageFolders = imageFolders;
            mllDir.setVisibility(mImageFolders == null || mImageFolders.isEmpty() ? View.GONE : View.VISIBLE);
            imagePicker.setImageFolders(imageFolders);
            if (imageFolders == null || imageFolders.isEmpty()) {
                mRecyclerAdapter.refreshData(null);
            } else {
                mRecyclerAdapter.refreshData(imageFolders.get(0).images);
            }
            mImageFolderAdapter.refreshData(imageFolders);
        });
    }

    @Override
    public void onImageItemClick(View view, ImageItem imageItem, int position) {
        //根据是否有相机按钮确定位置
        position = imagePicker.isShowCamera() ? position - 1 : position;
        ArrayList<ImageItem> imageItems = imagePicker.getCurrentImageFolderItems();

        if (imageItem.isVideo) {
            if (imageItems != null && imageItems.size() > position) {
                imagePicker.addSelectedImageItem(position, imageItems.get(position), true);
            }

            String videoPath = imageItem.path;
            Intent intent = new Intent();
            intent.putExtra("selectPath", videoPath);
            intent.putExtra("isVideo", true);
            setResult(ImagePicker.REQUEST_CODE_SELECT_PATH, intent);
            finish();
//            Intent intent = new Intent(MediaGridActivity.this, DeviceEditVideoActivity.class);
//            intent.putExtra(DeviceEditVideoActivity.Companion.getVIDEO_URI(), videoPath);
//            startActivityForResult(intent, ImagePicker.RESULT_CODE_EDIT_VIDEO);

        } else {
            if (imagePicker.isMultiMode()) {
                Intent intent = new Intent(MediaGridActivity.this, ImagePreviewActivity.class);
                intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position);

                /**
                 * 2017-03-20
                 *
                 * 依然采用弱引用进行解决，采用单例加锁方式处理
                 */

                // 据说这样会导致大量图片的时候崩溃
//            intent.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, imagePicker.getCurrentImageFolderItems());

                // 但采用弱引用会导致预览弱引用直接返回空指针
                DataHolder.getInstance().save(DataHolder.DH_CURRENT_IMAGE_FOLDER_ITEMS, imageItems);
                intent.putExtra(ImagePreviewActivity.ISORIGIN, isOrigin);
                startActivityForResult(intent, ImagePicker.REQUEST_CODE_PREVIEW);  //如果是多选，点击图片进入预览界面
            } else {
                imagePicker.clearSelectedImages();
                if (imageItems != null && imageItems.size() > position) {
                    imagePicker.addSelectedImageItem(position, imageItems.get(position), true);
                }

                Intent intent = new Intent();
                intent.putExtra("selectPath", imagePicker.getSelectedImages().get(0).path);
                intent.putExtra("isVideo", false);
                setResult(ImagePicker.REQUEST_CODE_SELECT_PATH, intent);
                finish();

//                if (imagePicker.isCrop()) {
//                    Intent intent = new Intent(MediaGridActivity.this, ImageCropActivity.class);
//                    startActivityForResult(intent, ImagePicker.REQUEST_CODE_CROP);  //单选需要裁剪，进入裁剪界面
//                } else {
//                    Intent intent = new Intent();
//                    intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
//                    setResult(ImagePicker.RESULT_CODE_ITEMS, intent);   //单选不需要裁剪，返回数据
//                    finish();
//                }
            }
        }
    }

    @Override
    public void onImageSelected(int position, ImageItem item, boolean isAdd) {
        if (imagePicker.getSelectImageCount() > 0) {
            mBtnOk.setText(getString(R.string.ip_select_complete, imagePicker.getSelectImageCount(), imagePicker.getSelectLimit()));
            mBtnOk.setEnabled(true);
            mBtnPre.setEnabled(true);
            mBtnPre.setText(getResources().getString(R.string.ip_preview_count, imagePicker.getSelectImageCount()));
            mBtnPre.setTextColor(ContextCompat.getColor(this, R.color.ip_text_primary_inverted));
            mBtnOk.setTextColor(ContextCompat.getColor(this, R.color.ip_text_primary_inverted));
        } else {
            mBtnOk.setText(getString(R.string.ip_complete));
            mBtnOk.setEnabled(false);
            mBtnPre.setEnabled(false);
            mBtnPre.setText(getResources().getString(R.string.ip_preview));
            mBtnPre.setTextColor(ContextCompat.getColor(this, R.color.ip_text_secondary_inverted));
            mBtnOk.setTextColor(ContextCompat.getColor(this, R.color.ip_text_secondary_inverted));
        }
//        mImageGridAdapter.notifyDataSetChanged();
//        mRecyclerAdapter.notifyItemChanged(position); // 17/4/21 fix the position while click img to preview
//        mRecyclerAdapter.notifyItemChanged(position + (imagePicker.isShowCamera() ? 1 : 0));// 17/4/24  fix the position while click right bottom preview button
        if (item == null) {
            return;
        }
        for (int i = imagePicker.isShowCamera() ? 1 : 0; i < mRecyclerAdapter.getItemCount(); i++) {
            if (mRecyclerAdapter.getItem(i) == null || TextUtils.isEmpty(mRecyclerAdapter.getItem(i).path)) {
                continue;
            }
            if (mRecyclerAdapter.getItem(i).path.equals(item.path)) {
                mRecyclerAdapter.notifyItemChanged(i);
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (data != null && data.getExtras() != null) {
//            if (resultCode == ImagePicker.RESULT_CODE_BACK) {
//                isOrigin = data.getBooleanExtra(ImagePreviewActivity.ISORIGIN, false);
//            } else if (requestCode == ImagePicker.RESULT_CODE_EDIT_VIDEO) {
//                Uri uri = (Uri) data.getExtras().get(DeviceEditVideoActivity.Companion.getVIDEO_URI());
//                setResult(ImagePicker.RESULT_CODE_EDIT_VIDEO, data);
//                finish();
//            } else {
//                //从拍照界面返回
//                //点击 X , 没有选择照片
//                if (data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS) == null) {
//                    File imageFile = imagePicker.getTakeImageFile();
//                    if (imageFile != null && imageFile.exists() && imageFile.isFile() && imageFile.length() > 0) {
//                        startCropImage();
//                    } else {
//                        finish();
//                    }
//                } else {
//                    //说明是从裁剪页面过来的数据，直接返回就可以
//                    setResult(ImagePicker.RESULT_CODE_ITEMS, data);
//                    finish();
//                }
//            }
//        } else {
//            //如果是裁剪，因为裁剪指定了存储的Uri，所以返回的data一定为null
//            if (resultCode == RESULT_OK && requestCode == ImagePicker.REQUEST_CODE_TAKE) {
//                startCropImage();
//            } else if (directPhoto) {
//                finish();
//            }
//        }
    }

    /**
     * 裁剪图片
     */
    private void startCropImage() {
        //发送广播通知图片增加了
        ImagePicker.galleryAddPic(this, imagePicker.getTakeImageFile());
        /**
         * 2017-03-21 对机型做旋转处理
         */
        String path = imagePicker.getTakeImageFile().getAbsolutePath();

        ImageItem imageItem = new ImageItem();
        imageItem.path = path;
        imagePicker.clearSelectedImages();
        imagePicker.addSelectedImageItem(0, imageItem, true);
        if (imagePicker.isCrop()) {
            Intent intent = new Intent(MediaGridActivity.this, ImageCropActivity.class);
            startActivityForResult(intent, ImagePicker.REQUEST_CODE_CROP);  //单选需要裁剪，进入裁剪界面
        } else {
            Intent intent = new Intent();
            intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
            setResult(ImagePicker.RESULT_CODE_ITEMS, intent);   //多选不需要裁剪，返回数据
        }
    }
}