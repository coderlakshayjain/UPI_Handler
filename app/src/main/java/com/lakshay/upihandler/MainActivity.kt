package com.lakshay.upihandler

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.lakshay.upihandler.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Real-time validation with ViewBinding
        binding.nameEditText.addTextChangedListener {
            val name = it.toString().trim()
            binding.nameLayout.error = if (name.isEmpty()) "Name required" else null
        }

        binding.emailEditText.addTextChangedListener {
            val email = it.toString().trim()
            binding.emailLayout.error =
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Invalid email" else null
        }

        binding.phoneEditText.addTextChangedListener {
            val phone = it.toString().trim()
            if (phone.length != 10) {
                binding.phoneLayout.error = "Enter valid 10-digit phone number (excluding +91)"
            } else {
                binding.phoneLayout.error = null
            }
        }

        binding.amountEditText.addTextChangedListener {
            val amountText = it.toString().trim()
            val amount = amountText.toIntOrNull()
            binding.amountLayout.error =
                if (amount == null || amount <= 0) {
                    "Enter valid amount (whole number)"
                } else if (amount > 50000) {
                    "Maximum amount allowed is ₹50,000"
                } else null
        }

        binding.payButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val phone = binding.phoneEditText.text.toString().trim()
            val amountText = binding.amountEditText.text.toString().trim()
            val description = binding.descriptionEditText.text.toString().trim()
            val amount = amountText.toIntOrNull()
            val isAmountValid = amount != null && amount > 0 && amount <= 50000
            val isPhoneValid = phone.length == 10

            val isValid = listOf(
                binding.nameLayout.error,
                binding.emailLayout.error,
                binding.phoneLayout.error,
                binding.amountLayout.error
            ).all { it == null } &&
                    name.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && amountText.isNotEmpty() &&
                    isAmountValid && isPhoneValid
            if (isValid) {
                val amount = amountText.toInt()
                val upiId = "merchant@upi" // Replace with actual UPI ID
                val nameOfPayee = "Merchant Name"
                val note = description.ifEmpty { "UPI Payment" }

                val uri = Uri.Builder()
                    .scheme("upi")
                    .authority("pay")
                    .appendQueryParameter("pa", upiId)
                    .appendQueryParameter("pn", nameOfPayee)
                    .appendQueryParameter("tn", note)
                    .appendQueryParameter("am", amount.toString())
                    .appendQueryParameter("cu", "INR")
                    .build()

                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = uri

                val chooser = Intent.createChooser(intent, "Pay with")

                if (chooser.resolveActivity(packageManager) != null) {
                    startActivityForResult(chooser, 101)
                } else {
                    Toast.makeText(
                        this,
                        "No UPI app found. Please install one to continue.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, "Please fix all errors before proceeding.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            if (data != null) {
                val response = data.getStringExtra("response")
                if (response?.contains("SUCCESS", true) == true) {
                    Toast.makeText(this, "Transaction Successful!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Transaction Failed or Cancelled.", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(this, "No response from UPI app.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}