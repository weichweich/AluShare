package edu.kit.tm.pseprak2.alushare.view.adapter;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;

/**
 * A class that enables getting thumbnails from video files and using Picasso to set imageViews.
 *
 * Created by arthuranselm on 19.08.15.
 */
public class VideoRequestHandler extends RequestHandler{

    public static final String video_Scheme = "video";

    /**
     * Checks if the scheme of the uri in data equals video_scheme and return the boolean.
     * @param data  the request
     * @return  true if valid uri else false
     */
    @Override
    public boolean canHandleRequest(Request data) {
        String scheme = data.uri.getScheme();
        return video_Scheme.equals(scheme);
    }

    /**
     * Creates the result with the video thumbnail of the uri in the given request.
     * @param request  the request that contains the video uri
     * @param networkPolicy the networkPolicy
     * @return  the result for Picasso
     * @throws IOException
     */
    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(request.uri.getPath(),
                MediaStore.Images.Thumbnails.MINI_KIND);
        return new Result(bitmap, Picasso.LoadedFrom.DISK);
    }
}
