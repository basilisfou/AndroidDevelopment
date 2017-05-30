package RSS.ApiService;

import Model.Model_Image_Gallery.Rss;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Url;


/**
 * Created by vfour_000 on 31/10/2015.
 */
public interface ImageApiService {

    @GET
    Call<Rss> getGadgets(@Url String url);
}
