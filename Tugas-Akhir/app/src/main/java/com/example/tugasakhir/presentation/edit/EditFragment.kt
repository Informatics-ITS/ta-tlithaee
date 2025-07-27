package com.example.tugasakhir.presentation.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.tugasakhir.R

class EditFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageButton>(R.id.ibPen).setOnClickListener {
            (activity as? EditActivity)?.setEditMode(EditMode.PEN)
        }

        view.findViewById<ImageButton>(R.id.ibEraser).setOnClickListener {
            (activity as? EditActivity)?.setEditMode(EditMode.ERASER)
        }

        view.findViewById<ImageButton>(R.id.ibRuler).setOnClickListener {
            (activity as? EditActivity)?.setEditMode(EditMode.RULER)
        }
    }
}