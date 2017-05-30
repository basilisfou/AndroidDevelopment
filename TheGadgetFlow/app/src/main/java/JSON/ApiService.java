package JSON;
import Helper.Constants;
import Model.Categories;
import retrofit.Call;
import retrofit.http.GET;
/**
 * Define Get method for the JSON categories
 */
public interface ApiService {

    @GET(Constants.GET_CATEGORIES)
    Call<Categories> getCategories();
}
