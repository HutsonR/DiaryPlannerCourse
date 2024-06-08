package com.easyflow.diarycourse.features.feature_home.task.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.easyflow.diarycourse.databinding.FragmentTaskDurationDialogBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TaskDurationDialog: BottomSheetDialogFragment() {
    private var _binding: FragmentTaskDurationDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        setBottomDialogStyle()
        _binding = FragmentTaskDurationDialogBinding.inflate(inflater)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setBottomDialogStyle() {
        dialog?.setOnShowListener { dialog ->
            val layout: FrameLayout? = (dialog as BottomSheetDialog).
            findViewById(com.google.android.material.R.id.design_bottom_sheet)
            layout?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = false
                behavior.skipCollapsed = true
            }
        }
    }

    companion object {
        const val TAG = "TaskDateDialog"
    }
}