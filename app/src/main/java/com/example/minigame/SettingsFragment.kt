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
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult

class SettingsFragment : DialogFragment() {

    private lateinit var seekBackgroundVolume: SeekBar
    private lateinit var seekEffectVolume: SeekBar
    private lateinit var editNickname: EditText
    private lateinit var btnSaveSettings: Button
    private lateinit var imgProfile: ImageView

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            SharedPrefManager.setProfileImageUri(requireContext(), it.toString())
            Toast.makeText(requireContext(), "프로필 사진이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            Glide.with(this).load(it).into(imgProfile)

            setFragmentResult("profileImageChanged", bundleOf("uri" to it.toString()))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireContext().getSharedPreferences("minigame_prefs", Context.MODE_PRIVATE)

        seekBackgroundVolume = view.findViewById(R.id.seekBackgroundVolume)
        seekEffectVolume = view.findViewById(R.id.seekEffectVolume)
        editNickname = view.findViewById(R.id.editNickname)
        btnSaveSettings = view.findViewById(R.id.btnSaveSettings)
        imgProfile = view.findViewById(R.id.imgProfile)

        // 초기값 설정
        seekBackgroundVolume.progress = sharedPref.getInt("background_volume", 50)
        seekEffectVolume.progress = sharedPref.getInt("effect_volume", 50)
        editNickname.setText(sharedPref.getString("nickname", "Player"))
        SharedPrefManager.getProfileImageUri(requireContext())?.let { uriString ->
            Glide.with(this).load(Uri.parse(uriString)).into(imgProfile)
        }

        // 프로필 이미지 변경
        imgProfile.setOnClickListener { imagePickerLauncher.launch("image/*") }
        imgProfile.setOnClickListener {
           SoundEffectManager.playClick(requireContext())
           imagePickerLauncher.launch("image/*")
        }

        // 실시간 배경음 볼륨 조정
        seekBackgroundVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                sharedPref.edit().putInt("background_volume", progress).apply()
                BgmManager.setVolume(requireContext())
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // 실시간 효과음 볼륨 조정 및 샘플 재생
        seekEffectVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                sharedPref.edit().putInt("effect_volume", progress).apply()
                SoundEffectManager.playClick(requireContext())
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // 닉네임만 저장
        btnSaveSettings.setOnClickListener {
            sharedPref.edit()
                .putString("nickname", editNickname.text.toString())
                .apply()
            Toast.makeText(requireContext(), "설정이 저장되었습니다", Toast.LENGTH_SHORT).show()
            dismiss()
        }
        btnSaveSettings.setOnClickListener {
           SoundEffectManager.playClick(requireContext())
           sharedPref.edit()
               .putString("nickname", editNickname.text.toString())
               .apply()
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