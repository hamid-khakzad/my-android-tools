package cn.emagsoftware.ipc;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import cn.emagsoftware.util.OptionalExecutorTask;

/**
 * Created by Wendell on 13-11-11.
 */
public abstract class ServerService extends Service {

    private Map<String,Bundle> callbackResult = Collections.synchronizedMap(new HashMap<String, Bundle>());
    private Map<ClientInfo,Messenger> clients = new HashMap<ClientInfo, Messenger>();
    private OptionalExecutorTask<Object,Object,Object> filterClientsTask = null;
    private Messenger messenger = new Messenger(new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            processMessage(msg);
        }
    });

    @Override
    public void onCreate() {
        super.onCreate();
        filterClientsTask = new OptionalExecutorTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                while(!isCancelled())
                {
                    try {
                        Thread.sleep(12000);
                    }catch (InterruptedException e)
                    {
                    }
                    publishProgress();
                }
                return null;
            }
            @Override
            protected void onProgressUpdate(Object... values) {
                super.onProgressUpdate(values);
                if(isCancelled()) return;
                filterClients();
            }
        };
        filterClientsTask.execute();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        filterClientsTask.cancel(true);
    }

    protected abstract void onClientConnected(ClientInfo client);
    protected abstract void onClientDisconnected(ClientInfo client);
    protected abstract void onReceiveMessage(ClientInfo client,Bundle data,String token);

    private void filterClients()
    {
        Iterator<Map.Entry<ClientInfo,Messenger>> entries = clients.entrySet().iterator();
        List<ClientInfo> failClients = new ArrayList<ClientInfo>();
        while(entries.hasNext())
        {
            Map.Entry<ClientInfo,Messenger> entry = entries.next();
            if(!entry.getValue().getBinder().pingBinder())
            {
                failClients.add(entry.getKey());
                entries.remove();
            }
        }
        for(ClientInfo client:failClients)
        {
            onClientDisconnected(client);
        }
    }

    private void processMessage(Message msg)
    {
        Bundle msgData = msg.getData();
        msgData.setClassLoader(ClientInfo.class.getClassLoader());
        if(msg.what == 1)
        {
            ClientInfo client = msgData.getParcelable("CLIENT_INFO");
            clients.remove(client);
            clients.put(client,msg.replyTo);
            onClientConnected(client);
        }else if(msg.what == 2)
        {
            ClientInfo client = msgData.getParcelable("CLIENT_INFO");
            ClientInfo toClient = null;
            Iterator<ClientInfo> clientIter = clients.keySet().iterator();
            while(clientIter.hasNext())
            {
                ClientInfo curClient = clientIter.next();
                if(curClient.equals(client))
                {
                    toClient = curClient;
                    break;
                }
            }
            if(toClient != null)
            {
                Bundle data = msgData.getBundle("DATA");
                String srcToken = msgData.getString("SRC_TOKEN");
                if(srcToken == null)
                {
                    String token = msgData.getString("TOKEN");
                    onReceiveMessage(toClient,data,token);
                }else
                {
                    if(callbackResult.containsKey(srcToken))
                    {
                        callbackResult.put(srcToken,data);
                    }
                }
            }
        }
    }

    public ClientInfo[] getConnectedClients()
    {
        filterClients();
        Set<ClientInfo> clientSet = clients.keySet();
        ClientInfo[] clientArr = new ClientInfo[clientSet.size()];
        return clientSet.toArray(clientArr);
    }

    private void sendMessage(ClientInfo client,Bundle data,String token,String srcToken) throws RemoteException
    {
        Messenger curMessenger = clients.get(client);
        if(curMessenger == null) throw new RemoteException();
        Message msg = Message.obtain();
        msg.getData().putBundle("DATA",data);
        msg.getData().putString("TOKEN", token);
        if(srcToken != null) msg.getData().putString("SRC_TOKEN", srcToken);
        curMessenger.send(msg);
    }

    private String generateToken()
    {
        return UUID.randomUUID().toString();
    }

    public void sendMessage(ClientInfo client,Bundle data) throws RemoteException
    {
        if(client == null || data == null) throw new NullPointerException("client or data is null.");
        sendMessage(client,data,generateToken(),null);
    }

    public void sendMessage(ClientInfo client,Bundle data,final ReplyCallback callback,int timeout) throws RemoteException
    {
        if(client == null || data == null || callback == null) throw new NullPointerException("client or data or callback is null.");
        if(timeout < 0) throw new IllegalArgumentException("timeout is invalid.");
        final String token = generateToken();
        callbackResult.put(token,null);
        try
        {
            sendMessage(client,data,token,null);
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

    public void replyMessage(ClientInfo client,Bundle data,String srcToken) throws RemoteException
    {
        if(client == null || data == null || srcToken == null) throw new NullPointerException("client or data or srcToken is null.");
        sendMessage(client,data,generateToken(),srcToken);
    }

}
