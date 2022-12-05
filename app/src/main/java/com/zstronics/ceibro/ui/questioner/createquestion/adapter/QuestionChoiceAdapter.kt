package com.zstronics.ceibro.ui.questioner.createquestion.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.databinding.LayoutItemQuestionChoiceBinding
import com.zstronics.ceibro.ui.questioner.createquestion.interfaces.QuestionCreateListener

class QuestionChoiceAdapter constructor(
    val questionPosition: Int,
    val questionCreateListener: QuestionCreateListener?
) :
    RecyclerView.Adapter<QuestionChoiceAdapter.QuestionChoiceViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: String) -> Unit)? = null

    private val questionChoices: ArrayList<String> = arrayListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionChoiceViewHolder {
        return QuestionChoiceViewHolder(
            LayoutItemQuestionChoiceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: QuestionChoiceViewHolder, position: Int) {
        val watcher = MyCustomEditTextListener()
        watcher.updateAdapterPosition(position)
        holder.bind(questionChoices[position], watcher)
    }

    inner class MyCustomEditTextListener : TextWatcher {
        private var adapterPosition = 0

        fun updateAdapterPosition(adapterPosition: Int) {
            this.adapterPosition = adapterPosition
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(char: CharSequence?, p1: Int, p2: Int, p3: Int) {
            questionChoices[adapterPosition] = char.toString()
            questionCreateListener?.onQuestionChoiceTitleChange(
                questionPosition,
                adapterPosition,
                char.toString()
            )
        }

        override fun afterTextChanged(p0: Editable?) {
        }
    }

    override fun getItemCount(): Int {
        return questionChoices.size
    }

    fun setList(list: List<String>) {
        this.questionChoices.clear()
        this.questionChoices.addAll(list)
        notifyDataSetChanged()
    }

    inner class QuestionChoiceViewHolder(private val binding: LayoutItemQuestionChoiceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String, watcher: TextWatcher) {
            itemView.setOnClickListener {
                itemClickListener?.invoke(it, adapterPosition, item)
            }
            val id = position + 1
            with(binding) {

                choiceIdTV.text = id.toString()
                choiceET.setText(item)
                choiceET.setSelection(choiceET.length())
                deleteIV.setOnClickListener {
                    questionCreateListener?.onQuestionChoiceDelete(
                        questionPosition,
                        adapterPosition
                    )
                }
                choiceET.addTextChangedListener(watcher)
            }
        }
    }
}