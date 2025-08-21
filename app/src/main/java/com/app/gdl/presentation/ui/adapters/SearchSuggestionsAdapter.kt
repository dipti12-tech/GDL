package com.app.gdl.presentation.ui.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.gdl.R
import com.google.android.libraries.places.api.model.AutocompletePrediction

class SearchSuggestionsAdapter(
    private val onClick: (AutocompletePrediction) -> Unit
) : ListAdapter<AutocompletePrediction, SearchSuggestionsAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AutocompletePrediction>() {
            override fun areItemsTheSame(
                old: AutocompletePrediction,
                newItem: AutocompletePrediction
            ): Boolean {
                return old.placeId == newItem.placeId
            }

            override fun areContentsTheSame(
                old: AutocompletePrediction,
                newItem: AutocompletePrediction
            ): Boolean {
                return old.getFullText(null) == newItem.getFullText(null)
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val suggestionText: TextView = itemView.findViewById(R.id.tvSuggestionText)
        fun bind(prediction: AutocompletePrediction) {
            suggestionText.text = prediction.getFullText(null)
            itemView.setOnClickListener { onClick(prediction) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_suggestion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
