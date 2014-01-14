package cn.emagsoftware.ui;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by Wendell on 14-1-14.
 */
public final class ImageLoaderManager {

    private ImageLoaderManager(){}

    public static void init(ImageLoaderConfiguration configuration){
        ImageLoader.getInstance().init(configuration);
    }

    public static ImageLoader getImageLoader(){
        return ImageLoader.getInstance();
    }

}
