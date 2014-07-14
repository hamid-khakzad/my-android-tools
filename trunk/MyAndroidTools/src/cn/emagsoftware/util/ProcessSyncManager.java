package cn.emagsoftware.util;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.FileLockInterruptionException;

/**
 * Created by Wendell on 14-7-11.
 */
public final class ProcessSyncManager {

    private static FileLock mFileLock = null;
    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                super.run();
                FileLock fileLockPoint = mFileLock;
                if(fileLockPoint != null) {
                    try {
                        fileLockPoint.release();
                    }catch (IOException e) {
                        LogManager.logE(ProcessSyncManager.class, "release file lock failed.", e);
                    }
                }
            }
        });
    }

    private ProcessSyncManager(){}

    public static void run(Context context, Runnable runnable) throws IOException {
        try {
            run(context, runnable, 0);
        }catch (TimeoutException e) {
            throw new RuntimeException(e); //timeout若设置为永不超时，将不会发生TimeoutException
        }
    }

    public static void run(Context context, Runnable runnable, int timeout) throws IOException, TimeoutException {
        if(timeout < 0) throw new IllegalArgumentException("timeout < 0");
        File lockFile = context.getFileStreamPath("ProcessSyncManager.Default.lock");
        FileChannel fileChannel = new RandomAccessFile(lockFile, "rw").getChannel();
        FileLock fileLock = null;
        try {
            int curTime = 0;
            while(timeout == 0 || curTime < timeout) {
                synchronized (ProcessSyncManager.class) {
                    if(mFileLock == null)
                        mFileLock = fileLock = fileChannel.tryLock();
                }
                if(fileLock != null) break;
                try {
                    Thread.sleep(200);
                }catch (InterruptedException e) {
                    throw new FileLockInterruptionException();
                }
                curTime = curTime + 200;
            }
            if(fileLock == null) throw new TimeoutException("time out.");
            runnable.run();
        }finally {
            if(fileLock != null) {
                try {
                    fileLock.release();
                } finally {
                    mFileLock = null;
                }
            }
        }
    }

}
