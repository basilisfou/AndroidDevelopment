package RSS.ApiService;
import Helper.Constants;
import Model.Model_Gadget.MainRss;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by vasilis fouroulis on 11/11/2015.
 * Defines the Search Call, URL: http://thegadgetflow.com/?feed=search_feed&s=%s&paged=%d
 */
public interface SearchApiService {

    @GET(Constants.SEARCH_FEED)
    Call<MainRss> getSearchedGadgets(@Query("s")String searchTerm, @Query("paged")int page);
}
