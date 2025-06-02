package com.example.minigame

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide

class SettingsFragment : DialogFragment() {

    private lateinit var seekBackgroundVolume: SeekBar
    private lateinit var seekEffectVolume: SeekBar
    private lateinit var editNickname: EditText
    private lateinit var btnSaveSettings: Button
    private lateinit var imgProfile: ImageView

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            SharedPrefManager.setProfileImageUri(requireContext(), it.toString())
            Toast.makeText(requireContext(), "프로필 사진이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            Glide.with(this).load(it).into(imgProfile)
        }
    }

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
        imgProfile = view.findViewById(R.id.imgProfile)

        val sharedPref = requireContext().getSharedPreferences("minigame_prefs", Context.MODE_PRIVATE)
        seekBackgroundVolume.progress = sharedPref.getInt("background_volume", 50)
        seekEffectVolume.progress = sharedPref.getInt("effect_volume", 50)
        editNickname.setText(sharedPref.getString("nickname", "Player"))

        SharedPrefManager.getProfileImageUri(requireContext())?.let { uriString ->
            Glide.with(this).load(Uri.parse(uriString)).into(imgProfile)
        }

        imgProfile.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

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
