package com.samyak2403.iptvmine.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.samyak2403.iptvmine.R

class ContributorItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val avatarImage: ImageView
    private val nameText: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.item_contributor, this, true)
        avatarImage = findViewById(R.id.contributor_avatar)
        nameText = findViewById(R.id.contributor_name)
        orientation = VERTICAL
        setPadding(16, 16, 16, 16)
    }

    fun setContributor(name: String, avatarUrl: String) {
        nameText.text = name
        Glide.with(context)
            .load(avatarUrl)
            .circleCrop()
            .into(avatarImage)
    }
}
