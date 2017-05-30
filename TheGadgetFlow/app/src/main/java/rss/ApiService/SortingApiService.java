package RSS.ApiService;

import Helper.Constants;
import Model.Model_Gadget.MainRss;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by vfour_000 on 1/3/2016.
 */
public interface SortingApiService {

    @GET(Constants.FULL_FEED)
    Call<MainRss> getGadgets(@Query("gf-sortby")String sorting,@Query("paged")int page);
}
