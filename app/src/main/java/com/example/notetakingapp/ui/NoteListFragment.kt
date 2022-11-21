package com.example.notetakingapp.ui

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.notetakingapp.BaseApplication
import com.example.notetakingapp.MainActivity
import com.example.notetakingapp.R
import com.example.notetakingapp.data.Note
import com.example.notetakingapp.databinding.FragmentNoteListBinding
import com.example.notetakingapp.viewmodel.FilterType
import com.example.notetakingapp.viewmodel.NoteTakingViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "NoteListFragment"

class NoteListFragment : Fragment(), SearchView.OnQueryTextListener {

    private val viewModel: NoteTakingViewModel by activityViewModels {
        NoteTakingViewModel.NoteTakingViewModelFactory(
            (activity?.application as BaseApplication).database.noteDao()
        )
    }

    lateinit var mainActivity: MainActivity

    private var _binding: FragmentNoteListBinding? = null

    private val binding get() = _binding!!

    private val checkedCardViews = mutableListOf<MaterialCardView>()

    private var searchBar: SearchView? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let { mainActivity = it as MainActivity }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentNoteListBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val adapter = NoteListAdapter(
            clickListener = { note, card ->
                if(mainActivity.actionMode != null) {
                    card.isChecked = if(!card.isChecked){
                        checkNote(note, card)
                        true
                    } else {
                        uncheckNote(note, card)
                        if (viewModel.selectedNotesSize() == 0) {
                            mainActivity.actionMode!!.finish()
                        }
                        false
                    }
                } else {
                    val action = NoteListFragmentDirections
                        .actionNoteListingFragmentToEditFragment(note.id)
                    this.findNavController().navigate(action)
                }

            },
            longClickListener = {note, card ->
                Log.d("NoteListAdapter", note.title)
                card.isChecked = if(!card.isChecked) {
                    checkNote(note, card)
                    if(mainActivity.actionMode == null)
                        mainActivity.actionMode = mainActivity.startActionMode(callback)
                    true
                } else {
                    uncheckNote(note, card)
                    if(viewModel.selectedNotesSize() == 0)
                        if(mainActivity.actionMode != null)
                            mainActivity.actionMode!!.finish()

                    false
                }

                return@NoteListAdapter true
            }
        )

        binding.noteGridRecycleView.adapter = adapter
        viewModel.displayedNotes.observe(viewLifecycleOwner) { items ->
            items.let {
                adapter.submitList(it)
            }
        }
        viewModel.noteCount.observe(viewLifecycleOwner) {
            if(it == 0){
                binding.noNotesTextView.visibility = View.VISIBLE
            } else {
                binding.noNotesTextView.visibility = View.GONE
            }
        }

        binding.floatingActionButton.setOnClickListener {
            clearSearchBar()
            val action = NoteListFragmentDirections
                .actionNoteListingFragmentToEditFragment()
            this.findNavController().navigate(action)
        }

        setUpToolBar()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_menu_bar, menu)

        val searchManager = mainActivity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchBar = menu.findItem(R.id.search).actionView as SearchView
//        searchBar?.isIconifiedByDefault = false
        searchBar?.setSearchableInfo(searchManager.getSearchableInfo(mainActivity.componentName))
        searchBar?.setOnQueryTextListener(this)
        searchBar?.setOnCloseListener { closeVirtualKeyboard() }
        
//        searchBar?.setOnKeyListener { view, keyCode, _ ->
//            handleEnterKey(
//                view,
//                keyCode)
//        }
    }

    override fun onQueryTextChange(p0: String?): Boolean {
        viewModel.filterNotes(FilterType.CONTENTS, p0)
        return false
    }

    override fun onQueryTextSubmit(p0: String?): Boolean {
        return false
    }

//    private fun handleEnterKey(view: View, keyCode: Int): Boolean{
//        Log.d(TAG, "handled a key on searchBar")
//        if(keyCode == KeyEvent.KEYCODE_ENTER) {
//            Log.d(TAG,"entered enter key")
//            return closeVirtualKeyboard()
//        }
//        return false
//    }

    private fun clearSearchBar(){
        Log.d(TAG, "Tried to clear search bar")
        if(searchBar != null) {
            searchBar?.setQuery("", true)
        }
    }

    private fun closeVirtualKeyboard(): Boolean{
        return try {
            clearSearchBar()
            val inputMethodManager =
                mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(requireView().windowToken, 0)
            true
        } catch (e: Exception){
            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    private fun setUpToolBar() {
        val mainActivity = mainActivity as MainActivity
        val navigationView: NavigationView = mainActivity.findViewById(R.id.navigation_view)
        mainActivity.setSupportActionBar(mainActivity.findViewById(R.id.list_fragment_top_bar))
        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration = mainActivity.appBarConfiguration
        NavigationUI.setupActionBarWithNavController(mainActivity, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(navigationView, navController)

//        val toolbar: MaterialToolbar = mainActivity.findViewById(R.id.list_fragment_top_bar)
//        toolbar.setNavigationOnClickListener {
//            closeVirtualKeyboard()
//        }
        setHasOptionsMenu(true)
    }


    //TODO: Handle drag to reorder the list

    private fun checkNote(note: Note, cardView: MaterialCardView){
        viewModel.addSelectedNote(note)
        checkedCardViews.add(cardView)
    }
    private fun uncheckNote(note: Note, cardView: MaterialCardView){
        viewModel.removeSelectedNote(note)
        checkedCardViews.remove(cardView)
    }

    private fun uncheckNotes(){
        checkedCardViews.forEach { card ->
            card.isChecked = false
        }
    }

    private val callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            Log.d(TAG, "Entered Action Mode")
            val inflater = mode?.menuInflater
            inflater?.inflate(R.menu.contextual_action_bar, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId){
                R.id.delete_action_button -> {
                    viewModel.deleteSelectedNotes()
                    mainActivity.actionMode!!.finish()
                    return true
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            mainActivity.actionMode = null
            if (viewModel.selectedNotesSize() != 0)
                viewModel.removeAllSelectedNotes()
            uncheckNotes()
        }
    }
}