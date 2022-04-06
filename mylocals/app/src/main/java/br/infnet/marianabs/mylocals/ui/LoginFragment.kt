package br.infnet.marianabs.mylocals.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import br.infnet.marianabs.mylocals.HomeActivity
import br.infnet.marianabs.mylocals.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginFragment: Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        binding.loginButton.setOnClickListener { loginAccount() }
    }

    private fun loginAccount() {
        val email = binding.emailField.text.toString()
        val password = binding.passwordField.text.toString()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    Toast.makeText(activity, "Logged in as ${currentUser?.email}", Toast.LENGTH_SHORT)
                        .show()
                    startActivity(Intent(activity, HomeActivity::class.java))
                    activity?.finish()
                } else {
                    Toast.makeText(activity, "Login Failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}