package com.eszdman.photoncamera.gallery;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;


public class ImageAdapter extends PagerAdapter {
    private final Context context;
    private final File[] file;

    ImageAdapter(Context context, File[] file) {
        this.context = context;
        this.file = file;
    }

    @Override
    public int getCount() {
        return file.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Arrays.sort(file, (file1, file2) -> Long.compare(file2.lastModified(), file1.lastModified()));
        TouchImageView imageView = new TouchImageView(context);
        Glide
                .with(context)
                .load(file[position])
                .into(imageView);
        container.addView(imageView);
        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}


