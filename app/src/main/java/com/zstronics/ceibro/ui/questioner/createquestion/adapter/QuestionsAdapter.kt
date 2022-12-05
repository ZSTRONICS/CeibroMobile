package com.zstronics.ceibro.ui.questioner.createquestion.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.repos.chat.questionarie.Question
import com.zstronics.ceibro.databinding.LayoutItemQuestionBinding
import com.zstronics.ceibro.ui.questioner.createquestion.interfaces.QuestionCreateListener
import javax.inject.Inject

class QuestionsAdapter @Inject constructor() :
    RecyclerView.Adapter<QuestionsAdapter.QuestionViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: Question) -> Unit)? = null

    var questionCreateListener: QuestionCreateListener? = null

    private val questions: ArrayList<Question> = arrayListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        return QuestionViewHolder(
            LayoutItemQuestionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(questions[position])
    }

    override fun getItemCount(): Int {
        return questions.size
    }

    fun setList(list: List<Question>) {
        this.questions.clear()
        this.questions.addAll(list)
        notifyDataSetChanged()
    }

    private val questionTypes = arrayOf(
        "Multiple", "Single Choice", "Short Answer"
    )

    inner class QuestionViewHolder(private val binding: LayoutItemQuestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Question) {
            itemView.setOnClickListener {
                itemClickListener?.invoke(it, adapterPosition, item)
            }

            binding.addQuestionChoice.setOnClickListener {
                questionCreateListener?.onQuestionChoiceAdd(adapterPosition)
            }
            binding.questionTitleET.setText(item.questionTitle)
            binding.questionTitleET.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(char: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    questions[adapterPosition].questionTitle = char.toString()
                    questionCreateListener?.onQuestionTitleChange(adapterPosition, char.toString())
                }

                override fun afterTextChanged(p0: Editable?) {
                }
            })

            /* Set spinner adapter */

            val adapter =
                ArrayAdapter(
                    binding.questionTypeSpinner.context,
                    android.R.layout.simple_spinner_item,
                    questionTypes
                )

            adapter.setDropDownViewResource(
                android.R.layout
                    .simple_spinner_dropdown_item
            )

            binding.questionTypeSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        adapterView: AdapterView<*>?,
                        p1: View?,
                        position: Int,
                        p3: Long
                    ) {
                        val questionType = when (position) {
                            0 -> "multiple"
                            1 -> "checkbox"
                            2 -> "shortAnswer"
                            else -> "multiple"
                        }

                        questionCreateListener?.onQuestionTypeChange(
                            adapterPosition,
                            questionType
                        )

                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }
                }
            val selectedPosition = when (item.questionType) {
                "multiple" -> 0
                "checkbox" -> 1
                "shortAnswer" -> 2
                else -> 0
            }
            binding.questionTypeSpinner.adapter = adapter
            binding.questionTypeSpinner.setSelection(selectedPosition)

            /* END Set spinner adapter */

            /* Setting choices adapter */

            if (item.options.isNotEmpty()) {
                val questionChoicesAdapter =
                    QuestionChoiceAdapter(
                        adapterPosition,
                        questionCreateListener
                    )
                binding.choicesRV.adapter =questionChoicesAdapter
                questionChoicesAdapter.setList(item.options)
            }
        }
    }
}