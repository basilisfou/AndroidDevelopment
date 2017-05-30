package RSS.ApiService;

import Helper.Constants;
import Model.Model_Gadget.MainRss;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by vfour_000 on 11/11/2015.
 */
public interface ListApiService {

    @GET(Constants.FULL_FEED)
    Call<MainRss> getGadgets(@Query("cat")String category,@Query("paged") int page);
}
