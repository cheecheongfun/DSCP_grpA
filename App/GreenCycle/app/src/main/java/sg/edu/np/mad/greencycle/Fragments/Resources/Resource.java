package sg.edu.np.mad.greencycle.Fragments.Resources;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Resource{
    private int resourceid;
    private String resourceinfo,resourcetitle, resourcelink, imageurl;

    public int getResourceid() {
        return resourceid;
    }

    public void setResourceid(int resourceid) {
        this.resourceid = resourceid;
    }

    public String getResourceinfo() {
        return resourceinfo;
    }

    public void setResourceinfo(String resourceinfo) {
        this.resourceinfo = resourceinfo;
    }

    public String getResourcetitle() {
        return resourcetitle;
    }

    public void setResourcetitle(String resourcetitle) {
        this.resourcetitle = resourcetitle;
    }

    public String getResourcelink() {
        return resourcelink;
    }

    public void setResourcelink(String resourcelink) {
        this.resourcelink = resourcelink;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public Resource(int resourceid, String resourceinfo, String resourcetitle, String resourcelink, String imageurl) {
        this.resourceid = resourceid;
        this.resourceinfo = resourceinfo;
        this.resourcetitle = resourcetitle;
        this.resourcelink = resourcelink;
        this.imageurl = imageurl;
    }
}

