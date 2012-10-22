package cn.emagsoftware.net.wifi.direct;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

import android.os.Handler;
import android.os.Looper;
import cn.emagsoftware.util.LogManager;
import cn.emagsoftware.util.StringUtilities;

public abstract class RemoteCallback implements Runnable
{

    private Selector selector = null;
    private Handler  handler  = new Handler(Looper.getMainLooper());

    void bindSelector(Selector selector)
    {
        if (selector == null)
            throw new NullPointerException();
        if (this.selector != null)
            throw new UnsupportedOperationException("bindSelector(Selector) can be called only once.");
        this.selector = selector;
    }

    @Override
    public final void run()
    {
        // TODO Auto-generated method stub
        while (true)
        {
            if (selector == null)
                return;
            try
            {
                selector.select();
            } catch (IOException e)
            {
                LogManager.logW(RemoteCallback.class, "running has been stopped.", e);
                return;
            }
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iter = keys.iterator();
            while (iter.hasNext())
            {
                SelectionKey key = iter.next();
                iter.remove(); // 当前事件要从keys中删去
                if (key.isAcceptable())
                {
                    SocketChannel sc = null;
                    try
                    {
                        sc = ((ServerSocketChannel) key.channel()).accept();
                        sc.configureBlocking(false);
                        sc.register(selector, SelectionKey.OP_READ);
                    } catch (final Exception e)
                    {
                        try
                        {
                            if (sc != null)
                                sc.close();
                        } catch (final IOException e1)
                        {
                            handler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    // TODO Auto-generated method stub
                                    onAcceptedFailed(e1);
                                }
                            });
                            return;
                        }
                        handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                // TODO Auto-generated method stub
                                onAcceptedFailed(e);
                            }
                        });
                    }
                } else if (key.isConnectable())
                {
                    final RemoteUser user = (RemoteUser) key.attachment();
                    SocketChannel sc = null;
                    try
                    {
                        sc = (SocketChannel) key.channel();
                        if (sc.isConnectionPending())
                            sc.finishConnect();
                    } catch (final IOException e)
                    {
                        key.cancel();
                        try
                        {
                            if (sc != null)
                                sc.close();
                        } catch (final IOException e1)
                        {
                            handler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    // TODO Auto-generated method stub
                                    onConnectedFailed(user, e1);
                                }
                            });
                            return;
                        }
                        handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                // TODO Auto-generated method stub
                                onConnectedFailed(user, e);
                            }
                        });
                        return;
                    }
                    user.setKey(key);
                    key.attach(new Object[] { user, "length", ByteBuffer.allocate(4) });
                    key.interestOps(SelectionKey.OP_READ);
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // TODO Auto-generated method stub
                            onConnected(user);
                        }
                    });
                } else if (key.isReadable())
                {
                    Object[] objArr = (Object[]) key.attachment();
                    if (objArr == null)
                    {
                        objArr = new Object[] { null, "length", ByteBuffer.allocate(4) };
                    }
                    final Object[] objs = objArr;
                    try
                    {
                        if (objs[2] instanceof ByteBuffer)
                        {
                            ByteBuffer bb = (ByteBuffer) objs[2];
                            SocketChannel sc = (SocketChannel) key.channel();
                            sc.read(bb);
                            if (!bb.hasRemaining())
                            {
                                bb.flip();
                                if (objs[1].equals("length"))
                                {
                                    key.attach(new Object[] { objs[0], "content", ByteBuffer.allocate(bb.getInt()) });
                                } else if (objs[1].equals("content"))
                                {
                                    String content = Charset.forName("UTF-8").newDecoder().decode(bb).toString();
                                    String[] contentArr = StringUtilities.parseFromCSV(content);
                                    if (contentArr[0].equals("userinfo"))
                                    {
                                        RemoteUser romoteUser = new RemoteUser(contentArr[1]);
                                        romoteUser.setKey(key);
                                        objs[0] = romoteUser;
                                        handler.post(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                // TODO Auto-generated method stub
                                                onAccepted((RemoteUser) objs[0]);
                                            }
                                        });
                                    } else if (contentArr[0].equals("transfer_request"))
                                    {
                                        final String path = contentArr[1];
                                        final long size = Long.parseLong(contentArr[2]);
                                        handler.post(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                // TODO Auto-generated method stub
                                                onTransferRequest((RemoteUser) objs[0], path, size);
                                            }
                                        });
                                    } else if (contentArr[0].equals("transfer_reply"))
                                    {
                                        final boolean allow = Boolean.parseBoolean(contentArr[1]);
                                        final String path = contentArr[2];
                                        final long size = Long.parseLong(contentArr[3]);
                                        handler.post(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                // TODO Auto-generated method stub
                                                onTransferReply((RemoteUser) objs[0], allow, path, size);
                                            }
                                        });
                                    } else if (contentArr[0].equals("transfer"))
                                    {

                                    }
                                }
                            }
                        }
                    } catch (IOException e)
                    {
                        LogManager.logE(RemoteCallback.class, "io error.", e);
                        key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4) });
                    }
                } else if (key.isWritable())
                {
                    Object[] objs = (Object[]) key.attachment();
                    SocketChannel sc = (SocketChannel) key.channel();
                    try
                    {
                        if (objs[1].equals("transfer_request"))
                        {
                            File file = (File) objs[2];
                            String msg = StringUtilities.concatByCSV(new String[] { "transfer_request", file.getAbsolutePath(), String.valueOf(file.length()) });
                            sc.write(ByteBuffer.wrap(msg.getBytes("UTF-8")));
                            key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4) });
                            key.interestOps(SelectionKey.OP_READ);
                        } else if (objs[1].equals("transfer_reply"))
                        {
                            String msg = StringUtilities.concatByCSV(new String[] { "transfer_reply", String.valueOf(objs[2]), String.valueOf(objs[3]), String.valueOf(objs[4]) });
                            sc.write(ByteBuffer.wrap(msg.getBytes("UTF-8")));
                            key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4) });
                            key.interestOps(SelectionKey.OP_READ);
                        } else if (objs[1].equals("transfer"))
                        {

                        }
                    } catch (IOException e)
                    {
                        LogManager.logE(RemoteCallback.class, "io error.", e);
                        key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4) });
                        key.interestOps(SelectionKey.OP_READ);
                    }
                }
            }
        }
    }

    public abstract void onAccepted(RemoteUser user);

    public abstract void onAcceptedFailed(Exception e);

    public abstract void onConnected(RemoteUser user);

    public abstract void onConnectedFailed(RemoteUser user, Exception e);

    public abstract void onTransferRequest(RemoteUser user, String path, long size);

    public abstract void onTransferReply(RemoteUser user, boolean allow, String path, long size);

    public abstract void onTransferProgress(RemoteUser user, String path, long size, int progress);

    public abstract void onTransferFailed(RemoteUser user, String path, long size, Exception e);

}
