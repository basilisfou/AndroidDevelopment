package RSS.ApiService;

import Helper.Constants;
import Model.Model_Gadget.MainRss;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by vasilis Fouroulis on 11/1/2016.
 */
public interface WishListApiService {

    @GET(Constants.WISH_LIST_FEED)
    Call<MainRss> getWishList(@Path("user")String user,@Query("paged")int page);

}
