package edu.cit.custodio.mdqueue.core.network

import edu.cit.custodio.mdqueue.features.auth.api.AuthApiService

import edu.cit.custodio.mdqueue.features.clinic.api.ClinicApiService
import edu.cit.custodio.mdqueue.features.queue.api.QueueApiService
import edu.cit.custodio.mdqueue.features.queue.api.QueueEntryApiService
import edu.cit.custodio.mdqueue.features.appointment.api.AppointmentApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Use 10.0.2.2 for Android Emulator to reach host machine's localhost
    // Change this to your PC's IP address if testing on a real device
    private const val BASE_URL = "http://10.0.2.2:8080/"

    var authToken: String? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
            authToken?.let {
                requestBuilder.header("Authorization", "Bearer $it")
            }
            chain.proceed(requestBuilder.build())
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApiService = retrofit.create(AuthApiService::class.java)
    val clinicApi: ClinicApiService = retrofit.create(ClinicApiService::class.java)
    val queueApi: QueueApiService = retrofit.create(QueueApiService::class.java)
    val queueEntryApi: QueueEntryApiService = retrofit.create(QueueEntryApiService::class.java)
    val appointmentApi: AppointmentApiService = retrofit.create(AppointmentApiService::class.java)
}
