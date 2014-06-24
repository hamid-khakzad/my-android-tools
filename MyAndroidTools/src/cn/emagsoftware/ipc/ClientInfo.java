package cn.emagsoftware.ipc;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Wendell on 13-11-10.
 */
public class ClientInfo implements Parcelable {

    String id;
    private Bundle info;

    public ClientInfo(Bundle info)
    {
        if(info == null) throw new NullPointerException();
        this.info = (Bundle)info.clone();
    }

    public Bundle getInfo()
    {
        return (Bundle)info.clone();
    }

    @Override
    public boolean equals(Object o) {
        super.equals(o);
        if(id == null || !(o instanceof ClientInfo)) return false;
        ClientInfo input = (ClientInfo)o;
        if(input.id == null) return false;
        return id.equals(input.id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if(id != null) parcel.writeString(id);
        parcel.writeBundle(info);
    }

    public static final Parcelable.Creator<ClientInfo> CREATOR = new Parcelable.Creator<ClientInfo>()
    {
        @Override
        public ClientInfo createFromParcel(Parcel parcel) {
            return new ClientInfo(parcel);
        }

        @Override
        public ClientInfo[] newArray(int i) {
            return new ClientInfo[i];
        }
    };

    private ClientInfo(Parcel parcel)
    {
        if(parcel.dataSize() > 1) id = parcel.readString();
        info = parcel.readBundle();
    }

}
