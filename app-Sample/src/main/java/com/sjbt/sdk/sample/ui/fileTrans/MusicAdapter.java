package com.sjbt.sdk.sample.ui.fileTrans;

import static com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.bumptech.glide.signature.ObjectKey;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sjbt.sdk.sample.R;
import com.sjbt.sdk.sample.base.Config;
import com.sjbt.sdk.sample.utils.AudioUtils;

import java.io.File;
import java.util.List;

public class MusicAdapter extends BaseQuickAdapter<LocalFileBean, MusicAdapter.ViewHolder> {

    public MusicAdapter(int layoutResId, @Nullable List<LocalFileBean> data) {
        super(layoutResId, data);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void convert(@NonNull ViewHolder helper, LocalFileBean item) {

        if (Config.FILE_TYPE_MP3.equals(item.getType())) {
            Uri albumArtUri = AudioUtils.getAlbumArtUri(item.getAlbumId());

            if (albumArtUri == null) {
                return;
            }

            Glide.with(mContext)
                    .asBitmap()
                    .transform(new RoundedCorners(15))
                    .load(albumArtUri)
                    .transition(withCrossFade(new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()))
                    .placeholder(R.mipmap.ic_music_album_40)
                    .signature(new ObjectKey(new File(item.getFileUrl()).lastModified()))
                    .error(R.mipmap.ic_music_album_40)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(helper.ivAlbumIcon);

        } else if (Config.FILE_TYPE_TXT.equals(item.getType())){
            Glide.with(mContext)
                    .asBitmap()
                    .transform(new RoundedCorners(15))
                    .load(R.mipmap.biu_icon_ebook)
                    .transition(withCrossFade(new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()))
                    .placeholder(R.mipmap.biu_icon_ebook)
                    .signature(new ObjectKey(new File(item.getFileUrl()).lastModified()))
                    .error(R.mipmap.biu_icon_ebook)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(helper.ivAlbumIcon);
        }else if (Config.FILE_TYPE_VIDEO.equals(item.getType())){
            Glide.with(mContext)
                    .asBitmap()
                    .transform(new RoundedCorners(15))
                    .load(R.mipmap.ic_book_video_40)
                    .transition(withCrossFade(new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()))
                    .placeholder(R.mipmap.ic_book_video_40)
                    .signature(new ObjectKey(new File(item.getFileUrl()).lastModified()))
                    .error(R.mipmap.ic_book_video_40)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(helper.ivAlbumIcon);
        }

        helper.tvName.setText(item.getFileName());
        helper.tvFileSize.setText(String.format(mContext.getString(R.string.file_size), item.getSize()));
        helper.ivSelect.setImageResource(item.isSelected() ? R.mipmap.biu_icon_check : R.drawable.shape_circle_stroke);

//        if(CacheDataHelper.INSTANCE.getActionSupportBean().supportSlowModel!=1){
//            helper.ivSent.setVisibility(item.getIsSent() ? View.VISIBLE : View.INVISIBLE);
//        }
    }

    public class ViewHolder extends BaseViewHolder {

        public ImageView ivAlbumIcon;
        public TextView tvName, tvFileSize;
        public ImageView ivSelect;

        public ViewHolder(View view) {
            super(view);
            ivAlbumIcon = view.findViewById(R.id.ivAlbumIcon);
            tvName = view.findViewById(R.id.tvFileName);
            tvFileSize = view.findViewById(R.id.tvFileSize);
            ivSelect = view.findViewById(R.id.ivSelect);
        }
    }

}
