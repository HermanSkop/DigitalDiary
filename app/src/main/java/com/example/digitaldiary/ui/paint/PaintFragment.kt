package com.example.digitaldiary.ui.paint

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.digitaldiary.R
import com.example.digitaldiary.ViewModel
import com.example.digitaldiary.databinding.FragmentPaintBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaintFragment : Fragment() {
    private lateinit var viewModel: ViewModel
    private lateinit var binding: FragmentPaintBinding
    private var uri: Uri? = null
    private val photoCallback = ActivityResultCallback<Boolean> {
        uri?.let { uri ->
            if (it) {
                requireContext().contentResolver.openInputStream(uri).use {
                    val bitmap = BitmapFactory.decodeStream(it)
                    binding.paintView.bitmap = bitmap
                }
            } else {
                requireContext().contentResolver.delete(uri, null, null)
                viewModel.navigateEditNote(null)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return FragmentPaintBinding.inflate(inflater, container, false).also {
            viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
            binding = it
        }.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val (images, newImageInfo) = setUpImage()
            uri = requireContext().contentResolver.insert(images, newImageInfo)

            registerForActivityResult(ActivityResultContracts.TakePicture(), photoCallback).launch(
                    uri
                )
        } else {
            Toast.makeText(
                requireContext(),
                requireContext().resources.getString(R.string.not_supported),
                Toast.LENGTH_LONG
            ).show()
        }

        viewModel.navigateTo.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { destination ->
                when (destination) {
                    ViewModel.Destination.NOTE -> {
                        findNavController().navigate(R.id.action_PaintFragment_to_NoteFragment)
                    }

                    else -> {
                        throw IllegalArgumentException("Unknown destination: $it")
                    }
                }
            }
        }
        setUpMenu()
    }

    private fun setUpMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.stop -> {
                        saveImage()
                        viewModel.navigateEditNote(null)
                        return true
                    }
                }
                return false
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_paint, menu)
            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }


    fun saveImage() {
        binding.paintView.getDrawing().let { bitmap ->
            val (images, newImageInfo) = setUpImage()
            requireContext().contentResolver.insert(images, newImageInfo)?.let { uri ->
                requireContext().contentResolver.openOutputStream(uri).use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it!!)
                }
                viewModel.attachImage(uri)
            }
        }
    }

    private fun setUpImage(): Pair<Uri, ContentValues> {
        val images = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val newImageInfo = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "image_${timeStamp}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return Pair(images, newImageInfo)
    }
}