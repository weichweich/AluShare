package edu.kit.tm.pseprak2.alushare.view.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import edu.kit.tm.pseprak2.alushare.R;

/**
 * Created by niklas on 07.09.15.
 */
public class TutorialFragment extends Fragment {
    int image;
    ImageView imageView;
    private Bitmap myBitmap;

    public static TutorialFragment createInstance(int image) {
        TutorialFragment fragment = new TutorialFragment();
        fragment.setImage(image);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial, container, false);
        imageView = (ImageView) view.findViewById(R.id.tutorial_image);
        setImageInViewPager();
        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (myBitmap != null) {
            myBitmap.recycle();
            myBitmap = null;
        }
        Runtime.getRuntime().gc();
    }

    public void setImage(int image) {
        this.image = image;
    }

    // Erzeugt Viewpager
    private void setImageInViewPager() {
        try {
            //if image size is too large. Need to scale as below code.
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            myBitmap = BitmapFactory.decodeResource(getResources(), image,
                    options);
            if (options.outWidth > 3000 || options.outHeight > 2000) {
                options.inSampleSize = 4;
            } else if (options.outWidth > 2000 || options.outHeight > 1500) {
                options.inSampleSize = 3;
            } else if (options.outWidth > 1000 || options.outHeight > 1000) {
                options.inSampleSize = 2;
            }
            options.inJustDecodeBounds = false;
            myBitmap = BitmapFactory.decodeResource(getResources(), image,
                    options);
            if (myBitmap != null) {
                try {
                    if (imageView != null) {
                        imageView.setImageBitmap(myBitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            System.gc();
        }
    }
}
