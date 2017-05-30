package RSS.ApiService;

import Helper.Constants;
import Model.Model_Gadget.MainRss;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by vfour_000 on 9/11/2015.
 */

public interface GadgetApiService {

    @GET(Constants.FULL_FEED)
    Call<MainRss> getGadgets(@Query("paged")int page);
}
