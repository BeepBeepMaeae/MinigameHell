package com.example.minigame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.DialogFragment

class GameDescriptionFragment : DialogFragment() {

    companion object {
        private const val ARG_IMAGE_RES = "image_res"

        fun newInstance(imageResId: Int): GameDescriptionFragment {
            val frag = GameDescriptionFragment()
            frag.arguments = Bundle().apply {
                putInt(ARG_IMAGE_RES, imageResId)
            }
            return frag
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_game_description, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val iv = view.findViewById<ImageView>(R.id.ivDescription)
        val btnClose = view.findViewById<Button>(R.id.btnClose)
        arguments?.getInt(ARG_IMAGE_RES)?.let { iv.setImageResource(it) }

        btnClose.setOnClickListener {
            // 클릭 시 효과음 재생
            SoundEffectManager.playClick(requireContext())
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window
            ?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}