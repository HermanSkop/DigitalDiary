package com.example.digitaldiary.ui.audio

import android.Manifest
import android.content.ContentValues
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.digitaldiary.MainActivity
import com.example.digitaldiary.R
import com.example.digitaldiary.ViewModel
import com.example.digitaldiary.databinding.FragmentAudioBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioFragment : Fragment() {
    private lateinit var audioFile: File
    private lateinit var timeStamp: String
    private lateinit var viewModel: ViewModel
    private lateinit var binding: FragmentAudioBinding
    private var mediaRecorder: MediaRecorder? = null
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                setUpMenu()
                setupMediaRecorder()
            } else {
                viewModel.navigateEditNote(null)
            }
        }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return FragmentAudioBinding.inflate(inflater, container, false).also {
            viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        viewModel.navigateTo.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { destination ->
                when (destination) {
                    ViewModel.Destination.NOTE -> findNavController().navigate(R.id.action_audioFragment_to_NoteFragment)
                    else -> throw IllegalArgumentException("Invalid destination $destination")
                }
            }
        }
    }

    private fun setUpMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.stop -> {
                        stopRecording()
                        return true
                    }
                }
                return false
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_audio, menu)
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        saveRecordingToMediaStore()
        viewModel.navigateEditNote(null)
    }

    private fun setupMediaRecorder() {
        timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        val recordingsDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File(requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC), "recordings")
        } else {
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "recordings")
        }
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs()
        }
        audioFile = File(recordingsDir, "recording_$timeStamp.3gp")

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioFile.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
            } catch (e: IOException) {
                Toast.makeText(requireContext(), getString(R.string.recording_preparation_failed), Toast.LENGTH_LONG).show()
                e.printStackTrace()
                return
            }
            start()
        }
    }

    private fun saveRecordingToMediaStore() {
        val resolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "recording_$timeStamp")
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/3gpp")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/recordings")
            }
        }
        val audioUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
        audioUri?.let { uri ->
            resolver.openOutputStream(uri)?.use { outputStream ->
                audioFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            viewModel.attachAudio(uri)
        }
        audioFile.delete()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaRecorder = null
    }
}
