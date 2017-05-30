package RSS.RestClient;

import Helper.Constants;
import retrofit.Retrofit;
import retrofit.SimpleXmlConverterFactory;
import RSS.ApiService.SortingApiService;

/**
 * Created by vasilis fouroulis on 1/3/2016.
 */
public class RestClientOldestGadget {
    private SortingApiService ApiService;
    //List Fragment
    public RestClientOldestGadget(){
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        ApiService = restAdapter.create(SortingApiService.class);
    }

    public SortingApiService getApiService() {
        return ApiService;
    }

}
