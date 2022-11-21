package com.example.notetakingapp.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.example.notetakingapp.BaseApplication
import com.example.notetakingapp.MainActivity
import com.example.notetakingapp.R
import com.example.notetakingapp.data.Note
import com.example.notetakingapp.databinding.FragmentEditBinding
import com.example.notetakingapp.viewmodel.NoteTakingViewModel
import com.google.android.material.navigation.NavigationView

private const val TAG = "EditFragment"

class EditFragment : Fragment() {
    private val navigationArgs: EditFragmentArgs by navArgs()
    private val viewModel: NoteTakingViewModel by activityViewModels {
        NoteTakingViewModel.NoteTakingViewModelFactory(
            (activity?.application as BaseApplication).database.noteDao()
        )
    }

    lateinit var mainActivity: FragmentActivity

    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!

    private lateinit var note: Note

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let { mainActivity = it }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun bind(note: Note) {
        binding.titleEditText.editText?.setText(note.title)
        binding.bodyEditText.editText?.setText(note.body)
        binding.categoryEditText.editText?.setText(note.category)
    }

    private fun saveNote() {
        if (viewModel.isEntryValid(
                binding.titleEditText.editText?.text.toString(),
                binding.bodyEditText.editText?.text.toString()
            )
        ) {
            viewModel.updateNote(
                note.id,
                binding.titleEditText.editText?.text.toString(),
                binding.bodyEditText.editText?.text.toString(),
                binding.categoryEditText.editText?.text.toString()
            )
        }
        findNavController().navigateUp()
    }

    private fun deleteNote() {
        viewModel.deleteNote(note)
        findNavController().navigateUp()
    }

    private fun newNote() {
        if (viewModel.isEntryValid(
                binding.titleEditText.editText?.text.toString(),
                binding.bodyEditText.editText?.text.toString()
            )
        ) {
            viewModel.addNewNote(
                binding.titleEditText.editText?.text.toString(),
                binding.bodyEditText.editText?.text.toString(),
                binding.categoryEditText.editText?.text.toString()
            )
        }
        findNavController().navigateUp()
    }

    private fun handleBack() {
        if (this::note.isInitialized) {
            saveNote()
        } else {
            newNote()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = navigationArgs.itemId
        if (id > 0) {
            viewModel.getNote(id).observe(this.viewLifecycleOwner) { selectedItem ->
                selectedItem?.let {
                    note = it
                    bind(note)
                }

            }

        }
        val callback = requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner) {
                handleBack()
            }
        callback.isEnabled = true

        setUpToolBar()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_menu_bar, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_top_button -> {
                if (this::note.isInitialized) {
                    deleteNote()
                } else {
                    findNavController().navigateUp()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setUpToolBar() {
        val mainActivity = mainActivity as MainActivity
        val navigationView: NavigationView = mainActivity.findViewById(R.id.navigation_view)
        val toolbar: Toolbar = mainActivity.findViewById(R.id.edit_fragment_top_bar)

        mainActivity.setSupportActionBar(toolbar)
        val navController = NavHostFragment.findNavController(this)
        NavigationUI.setupActionBarWithNavController(mainActivity, navController)
        NavigationUI.setupWithNavController(navigationView, navController)

        setHasOptionsMenu(true)
        toolbar.setNavigationOnClickListener {
            handleBack()
        }
    }

}