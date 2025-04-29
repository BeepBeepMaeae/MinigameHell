package com.example.minigame

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment

class PauseMenuFragment : DialogFragment() {

    interface PauseMenuListener {
        fun onResumeGame()
        fun onRetryGame()
        fun onQuitGame()
    }

    private var listener: PauseMenuListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PauseMenuListener) {
            listener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pause_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btnResume).setOnClickListener {
            listener?.onResumeGame()
            dismiss()
        }

        view.findViewById<Button>(R.id.btnRetry).setOnClickListener {
            listener?.onRetryGame()
            dismiss()
        }

        view.findViewById<Button>(R.id.btnQuit).setOnClickListener {
            listener?.onQuitGame()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}