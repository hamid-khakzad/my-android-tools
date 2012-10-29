package cn.emagsoftware.net.wifi.direct;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

import android.os.Handler;
import android.os.Looper;
import cn.emagsoftware.util.LogManager;
import cn.emagsoftware.util.MathUtilities;
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
                            continue;
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
                        try
                        {
                            user.close();
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
                            continue;
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
                        continue;
                    }
                    if (key == user.getKey())
                    {
                        key.attach(new Object[] { user, "info_accepted" });
                    } else if (key == user.getTransferKey())
                    {
                        key.attach(new Object[] { user, "transferkey_accepted" });
                    }
                    key.interestOps(SelectionKey.OP_WRITE);
                } else if (key.isReadable())
                {
                    Object[] objs = (Object[]) key.attachment();
                    if (objs == null)
                    {
                        objs = new Object[] { null, "length", ByteBuffer.allocate(4) };
                    }
                    SocketChannel sc = (SocketChannel) key.channel();
                    if (objs[2] instanceof ByteBuffer)
                    {
                        final RemoteUser remoteUser = (RemoteUser) objs[0];
                        ByteBuffer bb = (ByteBuffer) objs[2];
                        int len = 0;
                        try
                        {
                            len = sc.read(bb);
                        }catch(IOException e)
                        {
                            LogManager.logE(RemoteCallback.class, "reading remote failed.", e);
                        }
                        if(len == -1)
                        {
                            try
                            {
                                if(remoteUser == null)
                                {
                                    key.cancel();
                                    sc.close();
                                }else
                                {
                                    try
                                    {
                                        remoteUser.close();
                                    } finally
                                    {
                                        if(remoteUser.isConnectedComplete())
                                        {
                                            handler.post(new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    // TODO Auto-generated method stub
                                                    onDisconnected(remoteUser);
                                                }
                                            });
                                        }
                                    }
                                }
                            }catch(IOException e)
                            {
                                LogManager.logE(RemoteCallback.class, "close remote user failed when remote is close.", e);
                            }
                            continue;
                        }
                        if (!bb.hasRemaining())
                        {
                            bb.flip();
                            if (objs[1].equals("length"))
                            {
                                key.attach(new Object[] { remoteUser, "content", ByteBuffer.allocate(bb.getInt()) });
                            } else if (objs[1].equals("content"))
                            {
                                String content = null;
                                try
                                {
                                    content = Charset.forName("UTF-8").newDecoder().decode(bb).toString();
                                }catch(CharacterCodingException e)
                                {
                                    LogManager.logE(RemoteCallback.class, "decode remote content failed.", e);
                                    key.attach(new Object[] { remoteUser, "length", ByteBuffer.allocate(4) });
                                    continue;
                                }
                                String[] contentArr = StringUtilities.parseFromCSV(content);
                                if (contentArr[0].equals("info_accepted"))
                                {
                                    RemoteUser queryUser = null;
                                    Set<SelectionKey> skeys = selector.keys();
                                    for (SelectionKey curKey : skeys)
                                    {
                                        if (curKey == key)
                                            continue;
                                        Object[] curObjs = (Object[]) curKey.attachment();
                                        if (curObjs != null)
                                        {
                                            if (curObjs[0] != null)
                                            {
                                                RemoteUser curRemoteUser = (RemoteUser) curObjs[0];
                                                if (curRemoteUser.getTransferKey() != null)
                                                {
                                                    String curAddr = ((InetSocketAddress) ((SocketChannel) curKey.channel()).socket().getRemoteSocketAddress()).getAddress().getHostAddress();
                                                    String addr = ((InetSocketAddress) sc.socket().getRemoteSocketAddress()).getAddress().getHostAddress();
                                                    if (curAddr.equals(addr))
                                                    {
                                                        queryUser = curRemoteUser;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (queryUser == null)
                                    {
                                        queryUser = new RemoteUser(contentArr[1]);
                                        queryUser.setKey(key);
                                        key.attach(new Object[] { queryUser, "length", ByteBuffer.allocate(4) });
                                    } else
                                    {
                                        queryUser.setName(contentArr[1]);
                                        queryUser.setKey(key);
                                        final RemoteUser queryUserPoint = queryUser;
                                        handler.post(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                // TODO Auto-generated method stub
                                                onAccepted(queryUserPoint);
                                            }
                                        });
                                        key.attach(new Object[] { queryUser, "connected" });
                                        key.interestOps(SelectionKey.OP_WRITE);
                                    }
                                } else if (contentArr[0].equals("transferkey_accepted"))
                                {
                                    RemoteUser queryUser = null;
                                    Set<SelectionKey> skeys = selector.keys();
                                    for (SelectionKey curKey : skeys)
                                    {
                                        if (curKey == key)
                                            continue;
                                        Object[] curObjs = (Object[]) curKey.attachment();
                                        if (curObjs != null)
                                        {
                                            if (curObjs[0] != null)
                                            {
                                                RemoteUser curRemoteUser = (RemoteUser) curObjs[0];
                                                if (curRemoteUser.getKey() != null)
                                                {
                                                    String curAddr = ((InetSocketAddress) ((SocketChannel) curKey.channel()).socket().getRemoteSocketAddress()).getAddress().getHostAddress();
                                                    String addr = ((InetSocketAddress) sc.socket().getRemoteSocketAddress()).getAddress().getHostAddress();
                                                    if (curAddr.equals(addr))
                                                    {
                                                        queryUser = curRemoteUser;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (queryUser == null)
                                    {
                                        queryUser = new RemoteUser("");
                                        queryUser.setTransferKey(key);
                                        key.attach(new Object[] { queryUser, "length", ByteBuffer.allocate(4) });
                                    } else
                                    {
                                        queryUser.setTransferKey(key);
                                        final RemoteUser queryUserPoint = queryUser;
                                        handler.post(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                // TODO Auto-generated method stub
                                                onAccepted(queryUserPoint);
                                            }
                                        });
                                        queryUser.getKey().attach(new Object[] { queryUser, "connected" });
                                        queryUser.getKey().interestOps(SelectionKey.OP_WRITE);
                                        key.attach(new Object[] { queryUser, "length", ByteBuffer.allocate(4) });
                                    }
                                } else if (contentArr[0].equals("connected"))
                                {
                                    handler.post(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            // TODO Auto-generated method stub
                                            onConnected(remoteUser);
                                        }
                                    });
                                    key.attach(new Object[] { remoteUser, "length", ByteBuffer.allocate(4) });
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
                                            onTransferRequest(remoteUser, path, size);
                                        }
                                    });
                                    key.attach(new Object[] { remoteUser, "length", ByteBuffer.allocate(4) });
                                } else if (contentArr[0].equals("transfer_reply"))
                                {
                                    final boolean allow = Boolean.parseBoolean(contentArr[1]);
                                    final String path = contentArr[2];
                                    handler.post(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            // TODO Auto-generated method stub
                                            onTransferReply(remoteUser, allow, path);
                                        }
                                    });
                                    key.attach(new Object[] { remoteUser, "length", ByteBuffer.allocate(4) });
                                } else if (contentArr[0].equals("transfer"))
                                {
                                    final String path = contentArr[1];
                                    final long size = Long.parseLong(contentArr[2]);
                                    final String savingPath = onGetSavingPathInBackground(remoteUser, path, size);
                                    handler.post(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            // TODO Auto-generated method stub
                                            onTransferProgress(remoteUser, path, savingPath, size, 0);
                                        }
                                    });
                                    File file = new File(savingPath);
                                    File parentPath = file.getParentFile();
                                    if (parentPath != null && !parentPath.exists())
                                        if (!parentPath.mkdirs())
                                            throw new IOException("can not create saving path.");
                                    key.attach(new Object[] { remoteUser, "transfer_progress", new FileOutputStream(file).getChannel(), path, savingPath, size, 0, 0 });
                                }
                            }
                        }
                    } else if (objs[2] instanceof FileChannel)
                    {
                        if (objs[1].equals("transfer_progress"))
                        {
                            final RemoteUser remoteUser = (RemoteUser)objs[0];
                            final String path = String.valueOf(objs[3]);
                            final String savingPath = String.valueOf(objs[4]);
                            final long size = Long.parseLong(String.valueOf(objs[5]));
                            long curSize = Long.parseLong(String.valueOf(objs[6]));
                            ByteBuffer cache = ByteBuffer.allocate(2 * 1024);
                            int len = sc.read(cache);
                            if (len == -1)
                            {
                                if(size != curSize) throw new IOException("read to remote end but size is not equal.");
                                handler.post(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        // TODO Auto-generated method stub
                                        onTransferProgress(remoteUser,path,savingPath,size,100);
                                    }
                                });
                                key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4) });
                            }else
                            {
                                FileChannel channel = (FileChannel) objs[2];
                                curSize = curSize + cache.position();
                                cache.flip();
                                channel.write(cache);
                                int lastPublishProgress = Integer.parseInt(String.valueOf(objs[7]));
                                final int curProgress = (int)MathUtilities.mul(MathUtilities.div(curSize, size, 2), 100);  
                                if(curProgress - lastPublishProgress >= 5)
                                {
                                    lastPublishProgress = curProgress;
                                    handler.post(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            // TODO Auto-generated method stub
                                            onTransferProgress(remoteUser,path,savingPath,size,curProgress);
                                        }
                                    });
                                }
                                key.attach(new Object[] { objs[0], "transfer_progress", channel, path, savingPath, size, curSize, lastPublishProgress });
                            }
                        }
                    }
                } else if (key.isWritable())
                {
                    Object[] objs = (Object[]) key.attachment();
                    SocketChannel sc = (SocketChannel) key.channel();
                    if (objs[1].equals("info_accepted"))
                    {
                        RemoteUser remoteUser = (RemoteUser) objs[0];
                        String msg = StringUtilities.concatByCSV(new String[] { "info_accepted", remoteUser.getName() });
                        byte[] msgByte = msg.getBytes("UTF-8");
                        ByteBuffer sendBuff = ByteBuffer.allocate(4 + msgByte.length);
                        sendBuff.putInt(msgByte.length);
                        sendBuff.put(msgByte);
                        sendBuff.flip();
                        sc.write(sendBuff);
                        key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4) });
                        key.interestOps(SelectionKey.OP_READ);
                    } else if (objs[1].equals("transferkey_accepted"))
                    {
                        String msg = "transferkey_accepted";
                        byte[] msgByte = msg.getBytes("UTF-8");
                        ByteBuffer sendBuff = ByteBuffer.allocate(4 + msgByte.length);
                        sendBuff.putInt(msgByte.length);
                        sendBuff.put(msgByte);
                        sendBuff.flip();
                        sc.write(sendBuff);
                        key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4) });
                        key.interestOps(SelectionKey.OP_READ);
                    } else if (objs[1].equals("connected"))
                    {
                        String msg = "connected";
                        byte[] msgByte = msg.getBytes("UTF-8");
                        ByteBuffer sendBuff = ByteBuffer.allocate(4 + msgByte.length);
                        sendBuff.putInt(msgByte.length);
                        sendBuff.put(msgByte);
                        sendBuff.flip();
                        sc.write(sendBuff);
                        key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4) });
                        key.interestOps(SelectionKey.OP_READ);
                    } else if (objs[1].equals("transfer_request"))
                    {
                        File file = (File)objs[2];
                        if(!file.isFile()) throw new IOException("the file for transfer request is invalid.");
                        String msg = StringUtilities.concatByCSV(new String[] { "transfer_request", file.getAbsolutePath(), String.valueOf(file.length()) });
                        byte[] msgByte = msg.getBytes("UTF-8");
                        ByteBuffer sendBuff = ByteBuffer.allocate(4 + msgByte.length);
                        sendBuff.putInt(msgByte.length);
                        sendBuff.put(msgByte);
                        sendBuff.flip();
                        sc.write(sendBuff);
                        key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4) });
                        key.interestOps(SelectionKey.OP_READ);
                    } else if (objs[1].equals("transfer_reply"))
                    {
                        String msg = StringUtilities.concatByCSV(new String[] { "transfer_reply", String.valueOf(objs[2]), String.valueOf(objs[3]) });
                        byte[] msgByte = msg.getBytes("UTF-8");
                        ByteBuffer sendBuff = ByteBuffer.allocate(4 + msgByte.length);
                        sendBuff.putInt(msgByte.length);
                        sendBuff.put(msgByte);
                        sendBuff.flip();
                        sc.write(sendBuff);
                        key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4) });
                        key.interestOps(SelectionKey.OP_READ);
                    } else if (objs[1].equals("transfer"))
                    {
                        File file = (File)objs[2];
                        if(!file.isFile()) throw new IOException("the file for transfer is invalid.");
                        String path = file.getAbsolutePath();
                        String size = String.valueOf(file.length());
                        String msg = StringUtilities.concatByCSV(new String[] { "transfer", path, size });
                        byte[] msgByte = msg.getBytes("UTF-8");
                        ByteBuffer sendBuff = ByteBuffer.allocate(4 + msgByte.length);
                        sendBuff.putInt(msgByte.length);
                        sendBuff.put(msgByte);
                        sendBuff.flip();
                        sc.write(sendBuff);
                        key.attach(new Object[] { objs[0], "transfer_progress", new FileInputStream(file).getChannel(), path, size, 0, 0 });
                    } else if (objs[1].equals("transfer_progress"))
                    {
                        final RemoteUser remoteUser = (RemoteUser)objs[0];
                        FileChannel channel = (FileChannel) objs[2];
                        final String path = String.valueOf(objs[3]);
                        final long size = Long.parseLong(String.valueOf(objs[4]));
                        long curSize = Long.parseLong(String.valueOf(objs[5]));
                        ByteBuffer cache = ByteBuffer.allocate(2 * 1024);
                        int len = channel.read(cache);
                        if (len == -1)
                        {
                            if(size != curSize) throw new IOException("read to file end but size is not equal.");
                            handler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    // TODO Auto-generated method stub
                                    onTransferProgress(remoteUser,path,null,size,100);
                                }
                            });
                            key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4) });
                            key.interestOps(SelectionKey.OP_READ);
                        }else
                        {
                            curSize = curSize + cache.position();
                            cache.flip();
                            sc.write(cache);
                            int lastPublishProgress = Integer.parseInt(String.valueOf(objs[6]));
                            final int curProgress = (int)MathUtilities.mul(MathUtilities.div(curSize, size, 2), 100);  
                            if(curProgress - lastPublishProgress >= 5)
                            {
                                lastPublishProgress = curProgress;
                                handler.post(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        // TODO Auto-generated method stub
                                        onTransferProgress(remoteUser,path,null,size,curProgress);
                                    }
                                });
                            }
                            key.attach(new Object[] { objs[0], "transfer_progress", channel, path, size, curSize, lastPublishProgress });
                        }
                    }
                }
            }
        }
    }
    
    public abstract void onConnected(RemoteUser user);

    public abstract void onConnectedFailed(RemoteUser user, Exception e);

    public abstract void onTransferRequest(RemoteUser user, String path, long size);

    public abstract void onTransferReply(RemoteUser user, boolean allow, String path);

    public abstract String onGetSavingPathInBackground(RemoteUser user, String path, long size);
    
    /**
     * @param user
     * @param srcPath
     * @param savingPath 如果是发送文件的话，savingPath将传入null
     * @param size
     * @param progress 肯定会传入0和100用以给外部初始化下载和结束下载提供入口
     */
    public abstract void onTransferProgress(RemoteUser user, String srcPath, String savingPath, long size, int progress);
    
    /**
     * @param user
     * @param srcPath
     * @param savingPath 如果是发送文件的话，savingPath将传入null
     * @param size
     */
    public abstract void onTransferCancelled(RemoteUser user, String srcPath, String savingPath, long size);
    
    /**
     * @param user
     * @param srcPath
     * @param savingPath 如果是发送文件的话，savingPath将传入null
     * @param size
     * @param e
     */
    public abstract void onTransferFailed(RemoteUser user, String srcPath, String savingPath, long size, Exception e);
    
    public abstract void onDisconnected(RemoteUser user);
    
}
