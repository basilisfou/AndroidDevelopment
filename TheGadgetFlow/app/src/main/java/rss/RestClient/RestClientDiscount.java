package RSS.RestClient;

import Helper.Constants;
import retrofit.Retrofit;
import retrofit.SimpleXmlConverterFactory;
import RSS.ApiService.DiscountApiService;

/**
 * Created by vasilis fouroulis on 10/11/2015.
 */
public class RestClientDiscount {

    private DiscountApiService apiService;

    //home
    public RestClientDiscount(){
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        apiService = restAdapter.create(DiscountApiService.class);
    }

    public DiscountApiService getGadgetApiService() {
            return apiService;
        }

}
