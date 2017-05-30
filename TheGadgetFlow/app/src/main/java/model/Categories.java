package Model;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;


/**
 * map the categories to a list of Categories objects.
 */
/**
 ******************************
 *  8         8    888888888  *
 *   8       8     8          *
 *    8     8      888888888  *
 *     8   8       8          *
 *      8 8        8          *
 *       8     .   8         .*
 ******************************
 */
public class Categories implements Parcelable{

    private ArrayList<CategoryItem> categories;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(categories);

    }

    public static final Parcelable.Creator<Categories> CREATOR = new Parcelable.Creator<Categories>() {

        public Categories createFromParcel(Parcel in) {
            return new Categories(in);

        }

        public Categories[] newArray(int size){
            return new Categories[size];
        }
    };

    private Categories(Parcel in){

        categories = in.createTypedArrayList(CategoryItem.CREATOR);
    }

    public ArrayList<CategoryItem> getCategories() {
        return categories;
    }
}
