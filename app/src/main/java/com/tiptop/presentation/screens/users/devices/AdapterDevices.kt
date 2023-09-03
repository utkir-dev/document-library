package com.tiptop.presentation.screens.users.devices

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
import androidx.core.text.scale
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tiptop.app.common.Constants.LIB_VERSION
import com.tiptop.app.common.Variables.CURRENT_DEVICE_ID
import com.tiptop.app.common.huminize
import com.tiptop.data.models.local.DeviceLocal
import com.tiptop.databinding.ItemDeviceBinding


class AdapterDevices(
    val listener: DeviceClickListener
) :
    ListAdapter<DeviceLocal, AdapterDevices.Vh>(MyDiffUtil()) {
    inner class Vh(val v: ItemDeviceBinding) : RecyclerView.ViewHolder(v.root) {
        @SuppressLint("SetTextI18n")
        fun onBind(device: DeviceLocal, positionDevice: Int) {
            if (device.searchText.isNotEmpty()) {
                val spannableString = SpannableString(device.name)
                spannableString.setSpan(
                    ForegroundColorSpan(Color.RED),
                    0, device.searchText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                v.tvDeviceName.text = spannableString
            } else {
                v.tvDeviceName.text = device.name
            }
            val deviceType = if (device.tablet) "planshet" else "telefon"
            val spannableTextModel = SpannableStringBuilder()
                .append("(")
                .bold { scale(1.1f) { append(deviceType) } }
                .append(")  $LIB_VERSION")
            v.tvLibVersion.text = spannableTextModel

            if (device.id == CURRENT_DEVICE_ID) {
                val spannableTextData = SpannableStringBuilder()
                    .append("oxirgi sana: " + device.date.huminize() + "    (")
                    .bold { scale(1.3f) { append("men") } }
                    .append(")")
                v.tvDeviceDate.text = spannableTextData
            } else {
                v.tvDeviceDate.text = "oxirgi sana: " + device.date.huminize()
            }
            if (device.blocked) {
                v.tvAdmin.visibility = View.VISIBLE
                v.tvAdmin.text = "Taqiq"
                v.tvAdmin.setTextColor(Color.RED)
            } else if (device.admin) {
                v.tvAdmin.visibility = View.VISIBLE
                v.tvAdmin.text = "Admin"
                v.tvAdmin.setTextColor(Color.GREEN)
            } else {
                v.tvAdmin.visibility = View.GONE
            }
            v.ivMore.setOnClickListener {
                listener.onClick(device, v.ivMore, positionDevice)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(getItem(position), position)
    }

    interface DeviceClickListener {
        fun onClick(deviceLocal: DeviceLocal, v: View, positionDevice: Int)
    }

    class MyDiffUtil : DiffUtil.ItemCallback<DeviceLocal>() {
        override fun areItemsTheSame(oldItem: DeviceLocal, newItem: DeviceLocal): Boolean {
            return oldItem.id == newItem.id
//                    oldItem.userId == newItem.userId &&
//                    oldItem.name == newItem.name &&
//                    oldItem.tablet == newItem.tablet &&
//                    oldItem.libVersion == newItem.libVersion &&
//                    oldItem.blocked == newItem.blocked &&
//                    oldItem.admin == newItem.admin &&
//                    oldItem.date == newItem.date &&
//                    oldItem.dateAdded == newItem.dateAdded&&
//                    oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: DeviceLocal, newItem: DeviceLocal): Boolean {
            return oldItem == newItem
        }
    }
}