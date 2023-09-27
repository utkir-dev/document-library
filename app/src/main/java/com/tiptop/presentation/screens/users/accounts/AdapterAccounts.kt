package com.tiptop.presentation.screens.users.accounts

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.core.text.color
import androidx.core.text.scale
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tiptop.app.common.Variables
import com.tiptop.app.common.collapse
import com.tiptop.app.common.expand
import com.tiptop.app.common.huminize
import com.tiptop.data.models.local.UserLocal
import com.tiptop.databinding.ItemAccountBinding


class AdapterAccounts(
    val listener: UserClickListener
) :
    ListAdapter<UserLocal, AdapterAccounts.Vh>(MyDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(getItem(position), position)

    }

    inner class Vh(val itemV: ItemAccountBinding) : RecyclerView.ViewHolder(itemV.root) {
        @SuppressLint("SetTextI18n", "DefaultLocale", "ResourceAsColor")
        fun onBind(user: UserLocal, position: Int) {
            if (user.searchText.isNotEmpty() && user.email.contains(
                    user.searchText,
                    ignoreCase = true
                )
            ) {
                val spannableString = SpannableString(user.email)
                val startIndex = user.email.indexOf(user.searchText, ignoreCase = true)
                val endIndex = startIndex + user.searchText.length

                spannableString.setSpan(
                    ForegroundColorSpan(Color.RED),
                    startIndex,
                    endIndex,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                itemV.tvAccount.text = spannableString
            } else {
                itemV.tvAccount.text = user.email
            }

            itemV.tvLastDate.text = "oxirgi sana: ${user.date.huminize()}"

            if (user.id == Variables.CURRENT_USER_ID) {
                val spannableTextData = SpannableStringBuilder()
                    .append("qo'shilgan sana: ${user.dateAdded.huminize()}    (")
                    .bold { scale(1.3f) { append("men") } }
                    .append(")")
                itemV.tvAddedDate.text = spannableTextData
            } else {
                itemV.tvAddedDate.text = "qo'shilgan sana: ${user.dateAdded.huminize()}"
            }

            if (user.permitted) {
                itemV.btnCheck.visibility = View.GONE
                itemV.ivMoreUser.visibility = View.VISIBLE
            } else {
                itemV.btnCheck.visibility = View.VISIBLE
                itemV.ivMoreUser.visibility = View.GONE
            }

            itemV.ivMoreUser.setOnClickListener {
                listener.onClickMore(user, it)
            }
            itemV.btnCheck.setOnClickListener {
                listener.onClickCheck(user)
            }
            itemV.lUserContent.setOnClickListener {
                if (itemV.lDevices.visibility == View.GONE) {
                    listener.onClickRv(user, itemV.rvDevices, position)
                    expand(itemV.lDevices)
                } else {
                    collapse(itemV.lDevices)
                }
            }
        }
    }

    interface UserClickListener {
        fun onClickMore(user: UserLocal, v: View)
        fun onClickCheck(user: UserLocal)
        fun onClickRv(user: UserLocal, rv: RecyclerView, positionUser: Int)
    }

    class MyDiffUtil : DiffUtil.ItemCallback<UserLocal>() {
        override fun areItemsTheSame(oldItem: UserLocal, newItem: UserLocal): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserLocal, newItem: UserLocal): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        var type = 0
    }


}