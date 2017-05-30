package RSS.RestClient;
import Helper.Constants;
import retrofit.Retrofit;
import retrofit.SimpleXmlConverterFactory;
import RSS.ApiService.ImageApiService;


/**
 * Created by vfour_000 on 31/10/2015.
 */
public class RestClientImageGallery {

    private ImageApiService apiService;

    public RestClientImageGallery(){
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL_JSON_API)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        apiService = restAdapter.create(ImageApiService.class);
    }

    public ImageApiService getApiService(){
        return apiService;
    }
}