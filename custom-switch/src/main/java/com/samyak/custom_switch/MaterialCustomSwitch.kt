/*
 * Created by Samyak Kamble on 11/23/25, 09:20 PM
 *  Copyright (c) 2024. All rights reserved.
 *  Last modified 11/23/25, 12:18 PM
 */
package com.samyak.custom_switch



import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.materialswitch.MaterialSwitch


class MaterialCustomSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private var textHead: String = ""
    private var textOn: String = ""
    private var textOff: String = ""
    private var checked: Boolean = false

    private val textHeadView: TextView
    private val textDescView: TextView
    private val materialSwitch: MaterialSwitch

    private var onCheckChangedListener: OnCheckChangeListener? = null

    init {
        inflate(context, R.layout.material_custom_switch, this)

        textHeadView = findViewById(R.id.text_head)
        textDescView = findViewById(R.id.text_desc)
        materialSwitch = findViewById(R.id.materialSwitch)

        findViewById<LinearLayout>(R.id.root).setOnClickListener {
            materialSwitch.toggle()
        }

        materialSwitch.setOnCheckedChangeListener { _, isChecked ->
            textDescView.text = if (isChecked) textOn else textOff
            onCheckChangedListener?.onCheckChanged(isChecked)
        }

        if (attrs != null) {
            val a = context.obtainStyledAttributes(
                attrs,
                R.styleable.MaterialCustomSwitch,
                defStyle,
                0
            )

            textHead = a.getString(R.styleable.MaterialCustomSwitch_textHead) ?: ""
            textOn = a.getString(R.styleable.MaterialCustomSwitch_textOn) ?: ""
            textOff = a.getString(R.styleable.MaterialCustomSwitch_textOff) ?: ""
            checked = a.getBoolean(R.styleable.MaterialCustomSwitch_checked, false)

            textHeadView.text = textHead
            textDescView.text = if (checked) textOn else textOff
            materialSwitch.isChecked = checked

            a.recycle()
        }
    }

    fun setOnCheckChangeListener(listener: OnCheckChangeListener) {
        this.onCheckChangedListener = listener
    }

    fun setChecked(value: Boolean) {
        materialSwitch.isChecked = value
    }

    interface OnCheckChangeListener {
        fun onCheckChanged(isChecked: Boolean)
    }
}
