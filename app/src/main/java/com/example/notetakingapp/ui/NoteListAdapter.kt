package com.example.notetakingapp.ui


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notetakingapp.data.Note
import com.example.notetakingapp.databinding.NoteListItemBinding
import com.google.android.material.card.MaterialCardView

class NoteListAdapter(
    private val clickListener: (Note, MaterialCardView) -> Unit,
    private val longClickListener: (Note, MaterialCardView) -> Boolean
) : ListAdapter<Note, NoteListAdapter.ViewHolder>(NoteListAdapter.DiffCallback){

    companion object DiffCallback: DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }

    }

    class ViewHolder(
        private var binding: NoteListItemBinding
    ): RecyclerView.ViewHolder(binding.root){
        fun bind(note: Note){
            binding.note = note
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return NoteListAdapter.ViewHolder(
            NoteListItemBinding.inflate(layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = getItem(position)
        holder.itemView.setOnClickListener{
            clickListener(note, it as MaterialCardView)
        }
        holder.itemView.setOnLongClickListener {
            longClickListener(note, it as MaterialCardView)
        }
        holder.bind(note)
    }
}