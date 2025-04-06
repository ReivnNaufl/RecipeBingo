import com.unluckygbs.recipebingo.data.SpoonacularApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SpoonacularClient {
    private const val BASE_URL = "https://api.spoonacular.com/"

    val apiService: SpoonacularApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpoonacularApiService::class.java)
    }
}
