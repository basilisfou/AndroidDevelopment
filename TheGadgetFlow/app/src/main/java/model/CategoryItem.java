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
public class CategoryItem implements Parcelable {

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

    public static final Parcelable.Creator<CategoryItem> CREATOR = new Parcelable.Creator<CategoryItem>() {

        public CategoryItem createFromParcel(Parcel in) {
            return new CategoryItem(in);

        }

        public CategoryItem[] newArray(int size){
            return new CategoryItem[size];
        }
    };

    private CategoryItem(Parcel in){
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
