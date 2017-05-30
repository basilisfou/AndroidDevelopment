package Model;

import android.os.Parcel;
import android.os.Parcelable;
/*
******************************
*  8         8    888888888  *
*   8       8     8          *
*    8     8      888888888  *
*     8   8       8          *
*      8 8        8          *
*       8     .   8         .*
******************************
* vassilis Fouroulis
 */
public class NavDrawerItem implements Parcelable {

    private String title;
    private String description;
    private String image;
    private String post_count;
    private String id;
    private String slug;
    private int parent;
    private String sortingValuel;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(image);
        dest.writeString(post_count);
        dest.writeString(id);
        dest.writeString(slug);
        dest.writeInt(parent);
        dest.writeString(sortingValuel);
    }

    public static final Parcelable.Creator<NavDrawerItem> CREATOR = new Parcelable.Creator<NavDrawerItem>() {

        public NavDrawerItem createFromParcel(Parcel in) {
            return new NavDrawerItem(in);

        }

        public NavDrawerItem[] newArray(int size){
            return new NavDrawerItem[size];
        }
    };

    private NavDrawerItem(Parcel in){
        title = in.readString();
        description = in.readString();
        image = in.readString();
        post_count = in.readString();
        id = in.readString();
        slug = in.readString();
        parent = in.readInt();
        sortingValuel = in.readString();
    }



    public String getTitle() {
        return title;
    }
    public String getPost_count(){return post_count;}
    public String getImage() {
        return image;
    }

    public String getSortingValuel() {
        return sortingValuel;
    }

    public void setSortingValuel(String sortingValuel) {
        this.sortingValuel = sortingValuel;
    }

    public String getId() {
        return id;
    }
}
