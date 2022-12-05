package com.project24.animexapp.ui.LoadingScreens

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.project24.animexapp.MainActivity
import com.project24.animexapp.R
import com.project24.animexapp.api.QuotesApiClient
import com.project24.animexapp.api.QuotesResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class WelcomeScreenActivity : AppCompatActivity() {

    private val timeToTransition: Long = 3300 // In milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_screen)

        getWeebWords()

        setupAnimation()
        setupTransitionToMainActivity()
    }

    private fun getWeebWords(){
        val client = QuotesApiClient.apiService.randomQuote()
        client.enqueue(object: Callback<QuotesResponse> {
            override fun onResponse(
                call: Call<QuotesResponse>,
                response: Response<QuotesResponse>
            ) {
                if(response.isSuccessful){
                    val quoteTextView = findViewById<TextView>(R.id.quote_textview)
                    val quoteByTextView = findViewById<TextView>(R.id.quote_by_textview)
                    val quoteData = response.body()!!

                    val fadeIn: Animation = AlphaAnimation(0.toFloat(), 1.toFloat())
                    fadeIn.interpolator = DecelerateInterpolator()
                    fadeIn.duration = timeToTransition

                    val fadeInAnimation = AnimationSet(false)
                    fadeInAnimation.addAnimation(fadeIn)

                    quoteTextView.text = quoteData.quote
                    quoteByTextView.text = quoteData.character

                    quoteTextView.animation = fadeInAnimation
                    quoteByTextView.animation = fadeInAnimation


                    //Log.d("QUOTE",""+ response.body()!!.quote)
                }
            }

            override fun onFailure(call: Call<QuotesResponse>, t: Throwable) {
                Log.e("API FAIL",""+t.message)
            }
        })
    }

    private fun setupAnimation() {
        val fadeIn: Animation = AlphaAnimation(0.toFloat(), 1.toFloat())
        fadeIn.interpolator = DecelerateInterpolator()
        fadeIn.duration = timeToTransition

        val fadeInAnimation = AnimationSet(false)
        fadeInAnimation.addAnimation(fadeIn)

        val appTitle = findViewById<ImageView>(R.id.appTitleLogo)
        appTitle.animation = fadeInAnimation

        val welcomeImage = findViewById<ImageView>(R.id.logoImageView)
        welcomeImage.animation = fadeInAnimation
    }

    private fun setupTransitionToMainActivity() {
        object : CountDownTimer(
            timeToTransition,
            50
        ) {
            override fun onTick(l: Long) {}
            override fun onFinish() {

                // Sets up animation transition to the next activity
                val bundle = ActivityOptionsCompat.makeCustomAnimation(
                    this@WelcomeScreenActivity,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in
                ).toBundle()
                val mainIntent = Intent(this@WelcomeScreenActivity, MainActivity::class.java)
                startActivity(mainIntent)
                finish()
            }
        }.start()
    }
}