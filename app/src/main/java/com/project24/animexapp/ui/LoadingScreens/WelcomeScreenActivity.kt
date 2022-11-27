package com.project24.animexapp.ui.LoadingScreens

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.project24.animexapp.MainActivity
import com.project24.animexapp.R


class WelcomeScreenActivity : AppCompatActivity() {

    private val timeToTransition: Long = 1000 // In milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_screen)

        setupAnimation()
        setupTransitionToMainActivity()
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
            timeToTransition + 1000,
            1000
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