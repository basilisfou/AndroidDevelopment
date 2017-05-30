package RSS.RestClient;

import Helper.Constants;
import retrofit.Retrofit;
import retrofit.SimpleXmlConverterFactory;
import RSS.ApiService.SortingCategoryService;

/**
 * Created by vfour_000 on 11/3/2016.
 */
public class RestClientSortingCategory {
    private SortingCategoryService ApiService;
    //List Fragment
    public RestClientSortingCategory(){
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        ApiService = restAdapter.create(SortingCategoryService.class);
    }

    public SortingCategoryService getApiService() {
        return ApiService;
    }
}
