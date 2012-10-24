package cn.emagsoftware.net.wifi.direct;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
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
                                if (contentArr[0].equals("info_accepted"))
                                {
                                    RemoteUser romoteUser = null;
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
                                                        romoteUser = curRemoteUser;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (romoteUser == null)
                                    {
                                        romoteUser = new RemoteUser(contentArr[1]);
                                        romoteUser.setKey(key);
                                        key.attach(new Object[] { romoteUser, "length", ByteBuffer.allocate(4) });
                                    } else
                                    {
                                        romoteUser.setName(contentArr[1]);
                                        romoteUser.setKey(key);
                                        final RemoteUser romoteUserPoint = romoteUser;
                                        handler.post(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                // TODO Auto-generated method stub
                                                onAccepted(romoteUserPoint);
                                            }
                                        });
                                        key.attach(new Object[] { romoteUser, "connected" });
                                        key.interestOps(SelectionKey.OP_WRITE);
                                    }
                                } else if (contentArr[0].equals("transferkey_accepted"))
                                {
                                    RemoteUser romoteUser = null;
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
                                                        romoteUser = curRemoteUser;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (romoteUser == null)
                                    {
                                        romoteUser = new RemoteUser("");
                                        romoteUser.setTransferKey(key);
                                        key.attach(new Object[] { romoteUser, "length", ByteBuffer.allocate(4) });
                                    } else
                                    {
                                        romoteUser.setTransferKey(key);
                                        final RemoteUser romoteUserPoint = romoteUser;
                                        handler.post(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                // TODO Auto-generated method stub
                                                onAccepted(romoteUserPoint);
                                            }
                                        });
                                        romoteUser.getKey().attach(new Object[] { romoteUser, "connected" });
                                        romoteUser.getKey().interestOps(SelectionKey.OP_WRITE);
                                        key.attach(new Object[] { romoteUser, "length", ByteBuffer.allocate(4) });
                                    }
                                } else if (contentArr[0].equals("connected"))
                                {
                                    final RemoteUser remoteUser = (RemoteUser) objs[0];
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
                                    final RemoteUser remoteUser = (RemoteUser) objs[0];
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
                                } else if (contentArr[0].equals("transfer_reply"))
                                {
                                    final RemoteUser remoteUser = (RemoteUser) objs[0];
                                    final boolean allow = Boolean.parseBoolean(contentArr[1]);
                                    final String path = contentArr[2];
                                    final long size = Long.parseLong(contentArr[3]);
                                    handler.post(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            // TODO Auto-generated method stub
                                            onTransferReply(remoteUser, allow, path, size);
                                        }
                                    });
                                } else if (contentArr[0].equals("transfer"))
                                {

                                }
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
                        File file = (File) objs[2];
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
                        String msg = StringUtilities.concatByCSV(new String[] { "transfer_reply", String.valueOf(objs[2]), String.valueOf(objs[3]), String.valueOf(objs[4]) });
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
