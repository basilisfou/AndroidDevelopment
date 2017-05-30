package RSS.ApiService;

import Helper.Constants;
import Model.Model_Gadget.MainRss;
import Model.Model_Image_Gallery.Rss;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;
import retrofit.http.Url;

/**
 * Created by Vasilis Fouroulis on 20/4/2017.
 */

public interface GetGadgetByID {

    @GET(Constants.GET_SINGLE_PRODUCT)
    Call<MainRss> getGadget(@Query("prid")String productId);
}
