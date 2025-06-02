package com.example.minigame

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class SettingsFragment : DialogFragment() {

    private lateinit var seekBackgroundVolume: SeekBar
    private lateinit var seekEffectVolume: SeekBar
    private lateinit var editNickname: EditText
    private lateinit var btnSaveSettings: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        seekBackgroundVolume = view.findViewById(R.id.seekBackgroundVolume)
        seekEffectVolume = view.findViewById(R.id.seekEffectVolume)
        editNickname = view.findViewById(R.id.editNickname)
        btnSaveSettings = view.findViewById(R.id.btnSaveSettings)

        val sharedPref =
            requireContext().getSharedPreferences("minigame_prefs", Context.MODE_PRIVATE)
        seekBackgroundVolume.progress = sharedPref.getInt("background_volume", 50)
        seekEffectVolume.progress = sharedPref.getInt("effect_volume", 50)
        editNickname.setText(sharedPref.getString("nickname", "Player"))

        btnSaveSettings.setOnClickListener {
            val editor = sharedPref.edit()
            editor.putInt("background_volume", seekBackgroundVolume.progress)
            editor.putInt("effect_volume", seekEffectVolume.progress)
            editor.putString("nickname", editNickname.text.toString())
            editor.apply()

            BgmManager.setVolume(requireContext())

            Toast.makeText(requireContext(), "설정이 저장되었습니다", Toast.LENGTH_SHORT).show()
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