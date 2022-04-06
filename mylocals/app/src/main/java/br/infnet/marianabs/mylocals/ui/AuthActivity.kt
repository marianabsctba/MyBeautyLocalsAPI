package br.infnet.marianabs.mylocals.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import br.infnet.marianabs.mylocals.R
import br.infnet.marianabs.mylocals.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener { toggleAuthModal(true, LoginFragment()) }
        binding.signupButton.setOnClickListener { toggleAuthModal(true, RegisterFragment()) }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.auth_frame, fragment)
        fragmentTransaction.commit()
    }

    private fun toggleAuthModal(show: Boolean, fragment: Fragment) {
        if (show) {
            replaceFragment(fragment)
            toggleMainViewChildren(false)
            binding.mainView.animate()
                .translationY(binding.authFrame.measuredHeight.toFloat() * -1f + 100f)
                .setDuration(500)
                .setListener(null)
            binding.authFrame.animate()
                .translationY(0f)
                .setDuration(300)
                .setListener(null)
            binding.backButton.setOnClickListener {
                toggleAuthModal(false, fragment)
            }
        } else {
            toggleMainViewChildren(true)
            binding.mainView.animate()
                .translationY(0f)
                .setDuration(300)
                .setListener(null)
            binding.authFrame.animate()
                .translationY(binding.authFrame.measuredHeight.toFloat())
                .setDuration(500)
                .setListener(null)
        }
    }

    private fun toggleMainViewChildren(show: Boolean) {
        if (show) {
            binding.logo.visibility = View.VISIBLE
            binding.loginButton.visibility = View.VISIBLE
            binding.signupButton.visibility = View.VISIBLE
        } else {
            binding.logo.visibility = View.GONE
            binding.loginButton.visibility = View.GONE
            binding.signupButton.visibility = View.GONE
        }
    }
}