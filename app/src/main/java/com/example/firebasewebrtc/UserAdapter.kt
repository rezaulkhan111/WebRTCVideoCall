package com.example.firebasewebrtc

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasewebrtc.databinding.ListItemUserDataBinding

class UserAdapter(
    private val iCallBack: UserInteraction
) : RecyclerView.Adapter<UserAdapter.UserVH>() {
    private var workingAreas: MutableList<UserModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserVH {
        return UserVH(
            ListItemUserDataBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setWorkingAreas(listWorkingArea: MutableList<UserModel>) {
        workingAreas = listWorkingArea
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: UserVH, position: Int) {
        try {
            val workingArea = workingAreas[position]
            holder.onBind(workingArea)

            holder.apply {
                binding.apply {
                    ivAudioCall.setOnClickListener {
                        iCallBack.onClickAudioCall(workingArea)
                    }

                    ivVideoCall.setOnClickListener {
                        iCallBack.onClickVideoCall(workingArea)
                    }
                }
            }
        } catch (ignored: Exception) {
        }
    }

    override fun getItemCount(): Int {
        return workingAreas.size
    }

    inner class UserVH(val binding: ListItemUserDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(workingArea: UserModel) {
            binding.apply {
                tvName.text = workingArea.calleeId
            }
        }
    }
}

interface UserInteraction {
    fun onClickAudioCall(workingArea: UserModel)
    fun onClickVideoCall(workingArea: UserModel)
}