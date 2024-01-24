package com.example.diarycourse.features.feature_home.note

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.example.diarycourse.App
import com.example.diarycourse.R
import com.example.diarycourse.databinding.FragmentNoteBinding
import com.example.diarycourse.databinding.FragmentScheduleBinding
import com.example.diarycourse.features.feature_home.schedule.ScheduleFragment
import com.example.diarycourse.features.feature_home.schedule.ScheduleViewModel
import dagger.Lazy
import javax.inject.Inject

class NoteFragment : Fragment() {
    private val TAG = "debugTag"
    private var _binding: FragmentNoteBinding? = null
    private val binding get() = _binding!!
    @Inject
    lateinit var noteViewModelFactory: Lazy<NoteViewModel.NoteViewModelFactory>
    private val viewModel: NoteViewModel by viewModels {
        noteViewModelFactory.get()
    }
    private var dateSelected: String = ""

    companion object {
        fun newInstance() = ScheduleFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoteBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentListener()
    }

    private fun setFragmentListener() {
        setFragmentResultListener("dateKeyNote") { key, bundle ->
            val requestValue = bundle.getString("dateSelected")
            Log.d("debugTag", "requestValue Note $requestValue")
            if (requestValue != null) {
                dateSelected = requestValue
            }
        }
    }
}