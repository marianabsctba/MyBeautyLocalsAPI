package br.infnet.marianabs.mylocals.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import br.infnet.marianabs.mylocals.HomeActivity
import br.infnet.marianabs.mylocals.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterFragment: Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        binding.registerButton.setOnClickListener { registerAccount() }
    }

    private fun registerAccount() {
        val fullname = binding.fullnameField.text.toString()
        val email = binding.emailField.text.toString()
        val password = binding.passwordField.text.toString()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest
                        .Builder()
                        .setDisplayName(fullname)
                        .build()

                    user!!.updateProfile(profileUpdates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(activity, "Registration Successful", Toast.LENGTH_SHORT)
                                    .show()
                                startActivity(Intent(activity, HomeActivity::class.java))
                                activity?.finish()
                            }
                        }
                } else {
                    Toast.makeText(activity, "Registration Failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}