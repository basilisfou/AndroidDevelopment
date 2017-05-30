package RSS.RestClient;

import Helper.Constants;
import retrofit.Retrofit;
import retrofit.SimpleXmlConverterFactory;
import RSS.ApiService.ListApiService;

/**
 * Created by vfour_000 on 11/11/2015.
 */
public class RestClientList {

    private ListApiService ApiService;
    //List Fragment
    public RestClientList(){
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        ApiService = restAdapter.create(ListApiService.class);
    }

    public ListApiService getApiService() {
        return ApiService;
    }

}
