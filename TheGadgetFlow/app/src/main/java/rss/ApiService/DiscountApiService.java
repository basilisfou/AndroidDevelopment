package RSS.ApiService;

import Helper.Constants;
import Model.Model_Gadget.MainRss;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by vfour_000 on 10/11/2015.
 */
public interface DiscountApiService {

    @GET(Constants.DISCOUNTS_FEED)
    Call<MainRss> getGadgets(@Query("paged")int page);
}
