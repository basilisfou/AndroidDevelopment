package RSS.RestClient;

import Helper.Constants;
import retrofit.Retrofit;
import retrofit.SimpleXmlConverterFactory;
import RSS.ApiService.SearchApiService;

/**
 * Created by vfour_000 on 11/11/2015.
 */
public class SearchRestApi {

    private SearchApiService ApiService;

    //home
    public SearchRestApi(){
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        ApiService = restAdapter.create(SearchApiService.class);
    }

    public SearchApiService getGadgetApiService() {
        return ApiService;
    }
}
