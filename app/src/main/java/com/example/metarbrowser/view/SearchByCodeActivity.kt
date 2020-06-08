package com.example.metarbrowser.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.metarbrowser.R
import com.example.metarbrowser.databinding.ActivitySearchByCodeBinding
import com.example.metarbrowser.viewmodel.SearchCodeViewModel

class SearchByCodeActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySearchByCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_by_code)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_by_code)
        val viewModel = SearchCodeViewModel()
        binding.searchViewModel = viewModel
        binding.detailsViewModel = viewModel.detailsViewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        viewModel.registerLifeCycleObserver(lifecycle)

        addEditTextListener()
    }

    private fun addEditTextListener() {

        binding.editCode.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(editable: Editable?) {

                var s = editable.toString()
                if (s != s.toUpperCase()) {
                    s = s.toUpperCase()
                    binding.editCode.setText(s)
                    binding.editCode.setSelection(binding.editCode.text.toString().length)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }
}
