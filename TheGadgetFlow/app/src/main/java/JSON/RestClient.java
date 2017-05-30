package JSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Helper.Constants;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 *  The Rest Client of JSON
 */
public class RestClient {
    private ApiService apiService; //the Interface
    /**
     * creating the JSON with GSON library
     */
    Gson gson = new GsonBuilder().registerTypeAdapterFactory(new ItemTypeAdapterFactory())
                                .create();

    public RestClient() {
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL_JSON_API)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        apiService = restAdapter.create(ApiService.class);
    }

    //Getting the service
    public ApiService getApiService()
    {
        return apiService;
    }
}
