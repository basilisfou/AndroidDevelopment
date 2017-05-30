package RSS.RestClient;

import Helper.Constants;
import retrofit.Retrofit;
import retrofit.SimpleXmlConverterFactory;
import RSS.ApiService.GadgetApiService;

/**
 * Created by vfour_000 on 9/11/2015.
 */
public class RestClientGadget {

    private GadgetApiService gadgetApiService;

    //home
    public RestClientGadget(){
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        gadgetApiService = restAdapter.create(GadgetApiService.class);
    }

    public GadgetApiService getGadgetApiService() {
        return gadgetApiService;
    }
}
