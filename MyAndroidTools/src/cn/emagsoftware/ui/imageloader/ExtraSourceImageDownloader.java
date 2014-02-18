package cn.emagsoftware.ui.imageloader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.emagsoftware.content.pm.PackageMgr;

/**
 * Created by Wendell on 14-2-18.
 */
public class ExtraSourceImageDownloader extends BaseImageDownloader {

    public static final String SCHEME_PACKAGE = "package";

    public ExtraSourceImageDownloader(Context context) {
        super(context);
    }

    public ExtraSourceImageDownloader(Context context, int connectTimeout, int readTimeout) {
        super(context, connectTimeout, readTimeout);
    }

    @Override
    protected InputStream getStreamFromOtherSource(String imageUri, Object extra) throws IOException {
        if(imageUri != null) {
            String packagePrefix = SCHEME_PACKAGE + "://";
            if(imageUri.toLowerCase().startsWith(packagePrefix)) {
                String cropUri = imageUri.substring(packagePrefix.length());
                ApplicationInfo appInfo = PackageMgr.getInstalledApplication(context,cropUri);
                if(appInfo == null)
                    throw new IOException("Package named '" + cropUri + "' is not installed.");
                BitmapDrawable iconDrawable = (BitmapDrawable)appInfo.loadIcon(context.getPackageManager());
                Bitmap bitmap = iconDrawable.getBitmap();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, os);
                return new ByteArrayInputStream(os.toByteArray());
            }
        }
        return super.getStreamFromOtherSource(imageUri, extra);
    }

}
