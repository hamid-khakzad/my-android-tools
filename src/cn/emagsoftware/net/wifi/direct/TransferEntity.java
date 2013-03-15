package cn.emagsoftware.net.wifi.direct;

public class TransferEntity
{
    private RemoteUser remoteUser;
    private String     sendPath;
    private String     savingPath;
    private long       size;
    private boolean    isSender;
    private String     extraDescription;
    private Object     tag;
    private boolean    isCancelled = false;

    TransferEntity()
    {
    }

    public RemoteUser getRemoteUser()
    {
        return remoteUser;
    }

    void setRemoteUser(RemoteUser remoteUser)
    {
        if (remoteUser == null)
            throw new NullPointerException();
        this.remoteUser = remoteUser;
    }

    public String getSendPath()
    {
        return sendPath;
    }

    void setSendPath(String sendPath)
    {
        if (sendPath == null)
            throw new NullPointerException();
        this.sendPath = sendPath;
    }

    public String getSavingPath()
    {
        return savingPath;
    }

    void setSavingPath(String savingPath)
    {
        if (savingPath == null)
            throw new NullPointerException();
        this.savingPath = savingPath;
    }

    public long getSize()
    {
        return size;
    }

    void setSize(long size)
    {
        if (size < 0)
            throw new IllegalArgumentException("size could not less than zero.");
        this.size = size;
    }

    public boolean isSender()
    {
        return isSender;
    }

    void setSender(boolean isSender)
    {
        this.isSender = isSender;
    }

    public String getExtraDescription()
    {
        return extraDescription;
    }

    void setExtraDescription(String extraDescription)
    {
        this.extraDescription = extraDescription;
    }

    public Object getTag()
    {
        return tag;
    }

    public void setTag(Object tag)
    {
        if (tag == null)
            throw new NullPointerException();
        this.tag = tag;
    }

    void setCancelFlag()
    {
        this.isCancelled = true;
    }

    boolean getCancelFlag()
    {
        return isCancelled;
    }

}
