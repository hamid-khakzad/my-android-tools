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
                        sc.register(selector, SelectionKey.OP_READ, new Object[] { null, "length", ByteBuffer.allocate(4) });
                    } catch (Exception e)
                    {
                        try
                        {
                            if (sc != null)
                                sc.close();
                        } catch (IOException e1)
                        {
                            LogManager.logE(RemoteCallback.class, "close socket channel failed.", e1);
                        }
                        LogManager.logE(RemoteCallback.class, "handle accept socket channel failed.", e);
                    }
                } else if (key.isConnectable())
                {
                    Object[] objs = (Object[]) key.attachment();
                    if (objs[1].equals("connect"))
                    {
                        final RemoteUser user = (RemoteUser) objs[0];
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
                                key.cancel();
                                if (sc != null)
                                    sc.close();
                            } catch (IOException e1)
                            {
                                LogManager.logE(RemoteCallback.class, "close socket channel failed.", e1);
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
                        key.attach(new Object[] { user, "info_send", objs[2] });
                        key.interestOps(SelectionKey.OP_WRITE);
                    } else if (objs[1].equals("transfer_connect"))
                    {
                        RemoteUser user = (RemoteUser) objs[0];
                        final TransferEntity transfer = (TransferEntity) objs[2];
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
                                key.cancel();
                                if (sc != null)
                                    sc.close();
                            } catch (IOException e1)
                            {
                                LogManager.logE(RemoteCallback.class, "close socket channel failed.", e1);
                            }
                            try
                            {
                                transfer.close();
                            } catch (IOException e1)
                            {
                                LogManager.logE(RemoteCallback.class, "close transfer entity failed.", e1);
                            }
                            handler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    // TODO Auto-generated method stub
                                    onTransferFailed(transfer, e);
                                }
                            });
                            continue;
                        }
                        key.attach(new Object[] { user, "transfer_send", transfer });
                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                } else if (key.isReadable())
                {
                    Object[] objs = (Object[]) key.attachment();
                    SocketChannel sc = (SocketChannel) key.channel();
                    if (objs[2] instanceof ByteBuffer)
                    {
                        final RemoteUser remoteUser = (RemoteUser) objs[0];
                        ByteBuffer bb = (ByteBuffer) objs[2];
                        int len = 0;
                        try
                        {
                            len = sc.read(bb);
                        } catch (IOException e)
                        {
                            LogManager.logE(RemoteCallback.class, "reading remote failed.", e);
                        }
                        if (len == -1)
                        {
                            try
                            {
                                if (remoteUser == null)
                                {
                                    key.cancel();
                                    sc.close();
                                } else
                                {
                                    try
                                    {
                                        remoteUser.close();
                                    } finally
                                    {
                                        handler.post(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                // TODO Auto-generated method stub
                                                onRemoteDisconnected(remoteUser);
                                            }
                                        });
                                    }
                                }
                            } catch (IOException e)
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
                                } catch (CharacterCodingException e)
                                {
                                    LogManager.logE(RemoteCallback.class, "decode remote content failed.", e);
                                    key.attach(new Object[] { remoteUser, "length", ByteBuffer.allocate(4) });
                                    continue;
                                }
                                String[] contentArr = StringUtilities.parseFromCSV(content);
                                if (contentArr[0].equals("info_send"))
                                {
                                    final RemoteUser remote = new RemoteUser(contentArr[1]);
                                    remote.setIp(((InetSocketAddress) sc.socket().getRemoteSocketAddress()).getAddress().getHostAddress());
                                    remote.setKey(key);
                                    handler.post(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            // TODO Auto-generated method stub
                                            onConnected(remote);
                                        }
                                    });
                                    key.attach(new Object[] { remote, "length", ByteBuffer.allocate(4) });
                                } else if (contentArr[0].equals("transfer_request"))
                                {
                                    if (remoteUser != null)
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
                                    }
                                    key.attach(new Object[] { remoteUser, "length", ByteBuffer.allocate(4) });
                                } else if (contentArr[0].equals("transfer_reply"))
                                {
                                    if (remoteUser != null)
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
                                    }
                                    key.attach(new Object[] { remoteUser, "length", ByteBuffer.allocate(4) });
                                } else if (contentArr[0].equals("transfer_send"))
                                {
                                    RemoteUser queryUser = null;
                                    Set<SelectionKey> skeys = selector.keys();
                                    for (SelectionKey curKey : skeys)
                                    {
                                        if (curKey == key)
                                            continue;
                                        Object[] curObjs = (Object[]) curKey.attachment();
                                        if (curObjs[0] != null)
                                        {
                                            RemoteUser curRemoteUser = (RemoteUser) curObjs[0];
                                            String addr = ((InetSocketAddress) sc.socket().getRemoteSocketAddress()).getAddress().getHostAddress();
                                            if (addr.equals(curRemoteUser.getIp()))
                                            {
                                                queryUser = curRemoteUser;
                                                break;
                                            }
                                        }
                                    }
                                    if (queryUser == null)
                                    {
                                        key.attach(new Object[] { remoteUser, "length", ByteBuffer.allocate(4) });
                                    } else
                                    {
                                        final TransferEntity transfer = new TransferEntity();
                                        transfer.setRemoteUser(queryUser);
                                        transfer.setSendPath(contentArr[1]);
                                        transfer.setSize(Long.parseLong(contentArr[2]));
                                        transfer.setSavingPath(onGetSavingPathInBackground(queryUser, transfer.getSendPath(), transfer.getSize()));
                                        transfer.setSelectionKey(key);
                                        queryUser.addTransfer(transfer);
                                        handler.post(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                // TODO Auto-generated method stub
                                                onTransferProgress(transfer, 0);
                                            }
                                        });
                                        try
                                        {
                                            File file = new File(transfer.getSavingPath());
                                            File parentPath = file.getParentFile();
                                            if (parentPath != null && !parentPath.exists())
                                                if (!parentPath.mkdirs())
                                                    throw new IOException("can not create saving path.");
                                            key.attach(new Object[] { queryUser, "transfer_progress", transfer, new FileOutputStream(file).getChannel(), 0, 0 });
                                        } catch (final IOException e)
                                        {
                                            try
                                            {
                                                transfer.close();
                                            } catch (IOException e1)
                                            {
                                                LogManager.logE(RemoteCallback.class, "close transfer entity failed.", e1);
                                            }
                                            handler.post(new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    // TODO Auto-generated method stub
                                                    onTransferFailed(transfer, e);
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    } else if (objs[2] instanceof TransferEntity)
                    {
                        if (objs[1].equals("transfer_progress"))
                        {
                            final TransferEntity transfer = (TransferEntity) objs[2];
                            FileChannel channel = (FileChannel) objs[3];
                            ByteBuffer cache = ByteBuffer.allocate(2 * 1024);
                            try
                            {
                                int len = sc.read(cache);
                                if (len == -1)
                                {
                                    try
                                    {
                                        channel.close();
                                    } catch (IOException e)
                                    {
                                        LogManager.logE(RemoteCallback.class, " close file channel failed.", e);
                                    }
                                    try
                                    {
                                        transfer.close();
                                    } catch (IOException e)
                                    {
                                        LogManager.logE(RemoteCallback.class, "close transfer entity failed.", e);
                                    }
                                    handler.post(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            // TODO Auto-generated method stub
                                            onRemoteTransferCancelled(transfer);
                                        }
                                    });
                                } else
                                {
                                    cache.flip();
                                    channel.write(cache);
                                    long curSize = Long.parseLong(String.valueOf(objs[4]));
                                    curSize = curSize + cache.position();
                                    if (curSize >= transfer.getSize())
                                    {
                                        try
                                        {
                                            channel.close();
                                        } catch (IOException e)
                                        {
                                            LogManager.logE(RemoteCallback.class, " close file channel failed.", e);
                                        }
                                        try
                                        {
                                            transfer.close();
                                        } catch (IOException e)
                                        {
                                            LogManager.logE(RemoteCallback.class, "close transfer entity failed.", e);
                                        }
                                        handler.post(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                // TODO Auto-generated method stub
                                                onTransferProgress(transfer, 100);
                                            }
                                        });
                                    } else
                                    {
                                        int lastPublishProgress = Integer.parseInt(String.valueOf(objs[5]));
                                        final int curProgress = (int) MathUtilities.mul(MathUtilities.div(curSize, transfer.getSize(), 2), 100);
                                        if (curProgress - lastPublishProgress >= 5)
                                        {
                                            lastPublishProgress = curProgress;
                                            handler.post(new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    // TODO Auto-generated method stub
                                                    onTransferProgress(transfer, curProgress);
                                                }
                                            });
                                        }
                                        key.attach(new Object[] { objs[0], "transfer_progress", transfer, channel, curSize, lastPublishProgress });
                                    }
                                }
                            } catch (final IOException e)
                            {
                                try
                                {
                                    channel.close();
                                } catch (IOException e1)
                                {
                                    LogManager.logE(RemoteCallback.class, " close file channel failed.", e1);
                                }
                                try
                                {
                                    transfer.close();
                                } catch (IOException e1)
                                {
                                    LogManager.logE(RemoteCallback.class, "close transfer entity failed.", e1);
                                }
                                handler.post(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        // TODO Auto-generated method stub
                                        onTransferFailed(transfer, e);
                                    }
                                });
                            }
                        }
                    }
                } else if (key.isWritable())
                {
                    Object[] objs = (Object[]) key.attachment();
                    SocketChannel sc = (SocketChannel) key.channel();
                    if (objs[1].equals("info_send"))
                    {
                        final RemoteUser remoteUser = (RemoteUser) objs[0];
                        try
                        {
                            ByteBuffer sendBuff = null;
                            User user = null;
                            if (objs[2] instanceof ByteBuffer)
                            {
                                sendBuff = (ByteBuffer) objs[2];
                                user = (User) objs[3];
                            } else
                            {
                                user = (User) objs[2];
                                String msg = StringUtilities.concatByCSV(new String[] { "info_send", user.getName() });
                                byte[] msgByte = msg.getBytes("UTF-8");
                                sendBuff = ByteBuffer.allocate(4 + msgByte.length);
                                sendBuff.putInt(msgByte.length);
                                sendBuff.put(msgByte);
                                sendBuff.flip();
                            }
                            sc.write(sendBuff);
                            if (sendBuff.hasRemaining())
                            {
                                key.attach(new Object[] { objs[0], "info_send", sendBuff, user });
                            } else
                            {
                                key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4) });
                                key.interestOps(SelectionKey.OP_READ);
                                user.acceptIfNecessary();
                            }
                        } catch (final IOException e)
                        {
                            try
                            {
                                key.cancel();
                                sc.close();
                            } catch (IOException e1)
                            {
                                LogManager.logE(RemoteCallback.class, "close socket channel failed.", e1);
                            }
                            handler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    // TODO Auto-generated method stub
                                    onConnectedFailed(remoteUser, e);
                                }
                            });
                            continue;
                        }
                        remoteUser.setKey(key);
                        handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                // TODO Auto-generated method stub
                                onConnected(remoteUser);
                            }
                        });
                    } else if (objs[1].equals("transfer_request"))
                    {
                        try
                        {
                            ByteBuffer sendBuff = null;
                            if (objs[2] instanceof ByteBuffer)
                            {
                                sendBuff = (ByteBuffer) objs[2];
                            } else
                            {
                                File file = (File) objs[2];
                                if (!file.isFile())
                                    throw new IOException("the file for transfer request is invalid.");
                                String msg = StringUtilities.concatByCSV(new String[] { "transfer_request", file.getAbsolutePath(), String.valueOf(file.length()) });
                                byte[] msgByte = msg.getBytes("UTF-8");
                                sendBuff = ByteBuffer.allocate(4 + msgByte.length);
                                sendBuff.putInt(msgByte.length);
                                sendBuff.put(msgByte);
                                sendBuff.flip();
                            }
                            sc.write(sendBuff);
                            if (sendBuff.hasRemaining())
                            {
                                key.attach(new Object[] { objs[0], "transfer_request", sendBuff });
                            } else
                            {
                                key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4) });
                                key.interestOps(SelectionKey.OP_READ);
                            }
                        } catch (IOException e)
                        {
                            LogManager.logE(RemoteCallback.class, "transfer request failed.", e);
                            key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4) });
                            key.interestOps(SelectionKey.OP_READ);
                        }
                    } else if (objs[1].equals("transfer_reply"))
                    {
                        try
                        {
                            ByteBuffer sendBuff = null;
                            if (objs[2] instanceof ByteBuffer)
                            {
                                sendBuff = (ByteBuffer) objs[2];
                            } else
                            {
                                String msg = StringUtilities.concatByCSV(new String[] { "transfer_reply", String.valueOf(objs[2]), String.valueOf(objs[3]) });
                                byte[] msgByte = msg.getBytes("UTF-8");
                                sendBuff = ByteBuffer.allocate(4 + msgByte.length);
                                sendBuff.putInt(msgByte.length);
                                sendBuff.put(msgByte);
                                sendBuff.flip();
                            }
                            sc.write(sendBuff);
                            if (sendBuff.hasRemaining())
                            {
                                key.attach(new Object[] { objs[0], "transfer_reply", sendBuff });
                            } else
                            {
                                key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4) });
                                key.interestOps(SelectionKey.OP_READ);
                            }
                        } catch (IOException e)
                        {
                            LogManager.logE(RemoteCallback.class, "transfer reply failed.", e);
                            key.attach(new Object[] { objs[0], "length", ByteBuffer.allocate(4) });
                            key.interestOps(SelectionKey.OP_READ);
                        }
                    } else if (objs[1].equals("transfer_send"))
                    {
                        TransferEntity transfer = null;
                        try
                        {
                            ByteBuffer sendBuff = null;
                            if (objs[2] instanceof ByteBuffer)
                            {
                                sendBuff = (ByteBuffer) objs[2];
                                transfer = (TransferEntity) objs[3];
                            } else
                            {
                                transfer = (TransferEntity) objs[2];
                                String msg = StringUtilities.concatByCSV(new String[] { "transfer_send", transfer.getSendPath(), String.valueOf(transfer.getSize()) });
                                byte[] msgByte = msg.getBytes("UTF-8");
                                sendBuff = ByteBuffer.allocate(4 + msgByte.length);
                                sendBuff.putInt(msgByte.length);
                                sendBuff.put(msgByte);
                                sendBuff.flip();
                            }
                            sc.write(sendBuff);
                            if (sendBuff.hasRemaining())
                            {
                                key.attach(new Object[] { objs[0], "transfer_send", sendBuff, transfer });
                            } else
                            {
                                key.attach(new Object[] { objs[0], "transfer_progress", transfer, new FileInputStream(transfer.getSendPath()).getChannel(), 0, 0 });
                            }
                        } catch (final IOException e)
                        {
                            try
                            {
                                key.cancel();
                                sc.close();
                            } catch (IOException e1)
                            {
                                LogManager.logE(RemoteCallback.class, "close socket channel failed.", e1);
                            }
                            try
                            {
                                transfer.close();
                            } catch (IOException e1)
                            {
                                LogManager.logE(RemoteCallback.class, "close transfer entity failed.", e1);
                            }
                            final TransferEntity transferPoint = transfer;
                            handler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    // TODO Auto-generated method stub
                                    onTransferFailed(transferPoint, e);
                                }
                            });
                            continue;
                        }
                        transfer.setSelectionKey(key);
                    } else if (objs[1].equals("transfer_progress"))
                    {
                        TransferEntity transfer = null;
                        FileChannel channel = null;
                        long curSize;
                        int lastPublishProgress;
                        try
                        {
                            ByteBuffer sendBuff = null;
                            if (objs[2] instanceof ByteBuffer)
                            {
                                sendBuff = (ByteBuffer) objs[2];
                                transfer = (TransferEntity) objs[3];
                                channel = (FileChannel) objs[4];
                                curSize = Long.parseLong(String.valueOf(objs[5]));
                                lastPublishProgress = Integer.parseInt(String.valueOf(objs[6]));
                            } else
                            {
                                transfer = (TransferEntity) objs[2];
                                channel = (FileChannel) objs[3];
                                curSize = Long.parseLong(String.valueOf(objs[4]));
                                lastPublishProgress = Integer.parseInt(String.valueOf(objs[5]));
                                sendBuff = ByteBuffer.allocate(2 * 1024);
                                int len = channel.read(sendBuff);
                                if (len == -1)
                                {
                                    try
                                    {
                                        transfer.close();
                                    } catch (IOException e)
                                    {
                                        LogManager.logE(RemoteCallback.class, "close transfer entity failed.", e);
                                    }
                                    try
                                    {
                                        channel.close();
                                    } catch (IOException e)
                                    {
                                        LogManager.logE(RemoteCallback.class, "close file channel failed.", e);
                                    }
                                    final TransferEntity transferPoint = transfer;
                                    handler.post(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            // TODO Auto-generated method stub
                                            onTransferProgress(transferPoint, 100);
                                        }
                                    });
                                    continue;
                                }
                                sendBuff.flip();
                            }
                            sc.write(sendBuff);
                            long tempCurSize = curSize + sendBuff.position();
                            final int curProgress = (int) MathUtilities.mul(MathUtilities.div(tempCurSize, transfer.getSize(), 2), 100);
                            if (curProgress - lastPublishProgress >= 5)
                            {
                                lastPublishProgress = curProgress;
                                final TransferEntity transferPoint = transfer;
                                handler.post(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        // TODO Auto-generated method stub
                                        onTransferProgress(transferPoint, curProgress);
                                    }
                                });
                            }
                            if (sendBuff.hasRemaining())
                            {
                                key.attach(new Object[] { objs[0], "transfer_progress", sendBuff, transfer, channel, curSize, lastPublishProgress });
                            } else
                            {
                                curSize = curSize + sendBuff.position();
                                key.attach(new Object[] { objs[0], "transfer_progress", transfer, channel, curSize, lastPublishProgress });
                            }
                        } catch (final IOException e)
                        {
                            try
                            {
                                transfer.close();
                            } catch (IOException e1)
                            {
                                LogManager.logE(RemoteCallback.class, "close transfer entity failed.", e1);
                            }
                            try
                            {
                                channel.close();
                            } catch (IOException e1)
                            {
                                LogManager.logE(RemoteCallback.class, "close file channel failed.", e1);
                            }
                            final TransferEntity transferPoint = transfer;
                            handler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    // TODO Auto-generated method stub
                                    onTransferFailed(transferPoint, e);
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    public abstract void onConnected(RemoteUser user);

    public abstract void onConnectedFailed(RemoteUser user, Exception e);

    public abstract void onRemoteDisconnected(RemoteUser user);

    public abstract void onTransferRequest(RemoteUser user, String sendPath, long size);

    public abstract void onTransferReply(RemoteUser user, boolean allow, String sendPath);

    public abstract String onGetSavingPathInBackground(RemoteUser user, String sendPath, long size);

    /**
     * @param transfer 如果是发送文件的话，transfer.getSavingPath()返回的值为null
     * @param progress 肯定会传入0和100用以给外部初始化下载和结束下载提供入口
     */
    public abstract void onTransferProgress(TransferEntity transfer, int progress);

    /**
     * @param transfer 如果是发送文件的话，transfer.getSavingPath()返回的值为null
     * @param e
     */
    public abstract void onTransferFailed(TransferEntity transfer, Exception e);

    /**
     * @param transfer 如果是发送文件的话，transfer.getSavingPath()返回的值为null
     */
    public abstract void onRemoteTransferCancelled(TransferEntity transfer);

}
