package cn.emagsoftware.ipc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cn.emagsoftware.util.OptionalExecutorTask;

/**
 * Created by Wendell on 13-11-10.
 */
public abstract class Client {

    private Map<String,Bundle> callbackResult = Collections.synchronizedMap(new HashMap<String, Bundle>());
    private Context context = null;
    private ClientInfo info = null;
    private Messenger send = null;
    private ServiceConnection connection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Messenger thisSend = new Messenger(iBinder);
            Message msg = Message.obtain(null,1);
            msg.getData().putParcelable("CLIENT_INFO",info);
            msg.replyTo = new Messenger(new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    Bundle bundle = msg.getData();
                    Bundle data = bundle.getBundle("DATA");
                    String srcToken = bundle.getString("SRC_TOKEN");
                    if(srcToken == null)
                    {
                        String token = bundle.getString("TOKEN");
                        onReceiveMessage(data,token);
                    }else
                    {
                        if(callbackResult.containsKey(srcToken))
                        {
                            callbackResult.put(srcToken,data);
                        }
                    }
                }
            });
            try
            {
                thisSend.send(msg);
            }catch (RemoteException e)
            {
                throw new RuntimeException(e); //这种错误无法兼容，且在刚刚连上时发送消息的出错概率较小，故运行时抛出
            }
            send = thisSend;
            Client.this.onServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            send = null;
            Client.this.onServiceDisconnected();
        }
    };

    public Client(Context context)
    {
        if(context == null) throw new NullPointerException();
        this.context = context;
        ClientInfo implInfo = onInitInfo();
        if(implInfo == null) throw new NullPointerException("onInitInfo() returns null.");
        if(implInfo.id != null) throw new IllegalStateException("ClientInfo from onInitInfo() should be the newly created.");
        implInfo.id = UUID.randomUUID().toString();
        this.info = implInfo;
    }

    protected abstract ClientInfo onInitInfo();
    protected abstract void onServiceConnected();
    protected abstract void onServiceDisconnected();
    protected abstract void onReceiveMessage(Bundle data,String token);

    public boolean isServiceConnected()
    {
        return send != null;
    }

    public boolean bindService(Intent serviceIntent)
    {
        return context.bindService(serviceIntent,connection,Context.BIND_AUTO_CREATE);
    }

    public void unbindService()
    {
        context.unbindService(connection);
    }

    private void sendMessage(Bundle data,String token,String srcToken) throws RemoteException
    {
        if(!isServiceConnected()) throw new RemoteException();
        Message msg = Message.obtain(null,2);
        msg.getData().putParcelable("CLIENT_INFO",info);
        msg.getData().putBundle("DATA",data);
        msg.getData().putString("TOKEN", token);
        if(srcToken != null) msg.getData().putString("SRC_TOKEN", srcToken);
        send.send(msg);
    }

    private String generateToken()
    {
        return UUID.randomUUID().toString();
    }

    public void sendMessage(Bundle data) throws RemoteException
    {
        if(data == null) throw new NullPointerException("data is null.");
        sendMessage(data,generateToken(),null);
    }

    public void sendMessage(Bundle data,final ReplyCallback callback,int timeout) throws RemoteException
    {
        if(data == null || callback == null) throw new NullPointerException("data or callback is null.");
        if(timeout < 0) throw new IllegalArgumentException("timeout is invalid.");
        final String token = generateToken();
        callbackResult.put(token,null);
        try
        {
            sendMessage(data,token,null);
        }catch (RemoteException e)
        {
            callbackResult.remove(token);
            throw e;
        }
        new OptionalExecutorTask<Integer,Object,Bundle>(){
            @Override
            protected Bundle doInBackground(Integer... params) {
                int curTime = 0;
                Bundle reply = null;
                while((reply = callbackResult.get(token)) == null && curTime < params[0])
                {
                    try
                    {
                        Thread.sleep(200);
                    }catch (InterruptedException e)
                    {
                    }
                    curTime = curTime + 200;
                }
                return reply;
            }

            @Override
            protected void onPostExecute(Bundle bundle) {
                super.onPostExecute(bundle);
                callbackResult.remove(token);
                if(bundle == null) callback.onTimeout();
                else callback.onReplyMessage(bundle);
            }
        }.execute(timeout);
    }

    public void replyMessage(Bundle data,String srcToken) throws RemoteException
    {
        if(data == null || srcToken == null) throw new NullPointerException("data or srcToken is null.");
        sendMessage(data,generateToken(),srcToken);
    }

}
