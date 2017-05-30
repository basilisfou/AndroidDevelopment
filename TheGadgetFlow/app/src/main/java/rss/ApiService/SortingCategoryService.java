package RSS.ApiService;

import Helper.Constants;
import Model.Model_Gadget.MainRss;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by vfour_000 on 11/3/2016.
 */
public interface SortingCategoryService {

    @GET(Constants.FULL_FEED)
    Call<MainRss> getGadgets(@Query("cat")String category,@Query("paged") int page,@Query("gf-sortby")String sorting);
}
