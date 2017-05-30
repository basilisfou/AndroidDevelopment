package RSS.RestClient;

import Helper.Constants;
import retrofit.Retrofit;
import retrofit.SimpleXmlConverterFactory;
import RSS.ApiService.WishListApiService;

/**
 * Created by vfour_000 on 11/1/2016.
 */
public class RestClientWishList {

    private WishListApiService ApiService;

    //home
    public RestClientWishList(){
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        ApiService = restAdapter.create(WishListApiService.class);
    }

    public WishListApiService getGadgetApiService() {
        return ApiService;
    }
}
